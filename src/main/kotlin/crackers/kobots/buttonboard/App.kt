/*
 * Copyright 2022-2023 by E. A. Graham, Jr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package crackers.kobots.buttonboard

import crackers.kobots.app.AppCommon
import crackers.kobots.buttonboard.TheActions.mqttClient
import crackers.kobots.buttonboard.buttons.BackBenchPicker
import crackers.kobots.buttonboard.buttons.FrontBenchPicker
import crackers.kobots.buttonboard.buttons.RotoRegulator
import crackers.kobots.buttonboard.environment.EnvironmentDisplay
import crackers.kobots.devices.expander.I2CMultiplexer
import crackers.kobots.parts.scheduleWithFixedDelay
import org.slf4j.LoggerFactory
import java.awt.Color
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

const val REMOTE_PI = "diozero.remote.hostname"

/**
 * Defines what various parts of the day are
 */
enum class Mode(val brightness: Float) {
    NONE(0f),
    NIGHT(.005f),
    MORNING(.02f),
    DAYTIME(0.05f),
    EVENING(.03f),
    ;

    fun isNight() = this == NIGHT || this == EVENING
}

private val logger = LoggerFactory.getLogger("ButtonBox")

private val _currentMode = AtomicReference(Mode.NONE)
var currentMode: Mode
    get() = _currentMode.get()
    @Synchronized
    set(m) {
        if (_currentMode.get() != m) {
            _currentMode.set(m)
            BackBenchPicker.selectMenu(m)
            // TODO handler shouldn't be exposed?
            m.brightness.let {
                FrontBenchPicker.keyHandler.brightness = it
                BackBenchPicker.keyHandler.brightness = it
            }
            // TODO or this?
            BackBenchPicker.currentMenu.displayMenu()
            FrontBenchPicker.currentMenu.displayMenu()
            RotoRegulator.setPixelColor()
        }
    }

private var _remote: Boolean = false
internal val isRemote: Boolean
    get() = _remote

internal val i2cMultiplexer: I2CMultiplexer by lazy { I2CMultiplexer() }

fun killAllTheThings() {
    TheActions.GripperActions.STOP()
    TheActions.ServoMaticActions.STOP()
    AppCommon.applicationRunning = false
}

/**
 * Uses NeoKey 1x4 as a HomeAssistant controller (and likely other things).
 */
fun main(args: Array<String>) {
    _remote = args.isNotEmpty().also { if (it) System.setProperty(REMOTE_PI, args[0]) }

    TheStrip.start()
    EnvironmentDisplay.start()
    i2cMultiplexer.use {
        FrontBenchPicker.start()
        BackBenchPicker.start()

        mqttClient.startAliveCheck()

        // start the "main" loop -- note that the Java scheduler is more CPU efficient than simply looping and waiting
        val theFuture = AppCommon.executor.scheduleWithFixedDelay(1.seconds, 50.milliseconds, ::modeAndKeyboardCheck)
        AppCommon.awaitTermination()
        theFuture.cancel(true)

        BackBenchPicker.keyHandler.buttonColors = listOf(Color.RED, Color.RED, Color.RED, Color.RED)
        logger.warn("Exiting ")

        FrontBenchPicker.stop()
        BackBenchPicker.stop()
        RotoRegulator.encoder.close()
    }
    EnvironmentDisplay.stop()
    TheStrip.stop()

    AppCommon.executor.shutdownNow()
    exitProcess(0)
}

private fun modeAndKeyboardCheck() {
    if (AppCommon.applicationRunning) {
        try {
            // adjust per time of day
            val hour = LocalTime.now().hour
            currentMode = when {
                hour in (0..6) -> Mode.NIGHT
                hour <= 8 -> Mode.MORNING
                hour <= 20 -> Mode.DAYTIME
                else -> if (currentMode != Mode.NIGHT) Mode.EVENING else currentMode
            }

            // *****************************
            // ***** READ BUTTONS HERE *****
            // because the sensor can be used as an independent control, check it first
            // ditto for the rotary encoder
            // *****************************
            GestureSensor.whatAmIDoing()
            RotoRegulator.readTwist()
            if (!BackBenchPicker.currentMenu.firstButton()) FrontBenchPicker.currentMenu.firstButton()
        } catch (e: Throwable) {
            logger.error("Error found - continuing", e)
        }
    }
}
