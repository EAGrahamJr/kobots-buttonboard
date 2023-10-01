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
import crackers.kobots.devices.expander.I2CMultiplexer
import org.slf4j.LoggerFactory
import java.awt.Color
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.exitProcess

const val REMOTE_PI = "diozero.remote.hostname"

/**
 * Defines what various parts of the day are
 */
enum class Mode(
    val brightness: Float = 0.1f,
) {
    NONE(0f),
    NIGHT(.01f),
    MORNING(.05f),
    DAYTIME,
    EVENING(.03f),
}

private val logger = LoggerFactory.getLogger("ButtonBox")

private val _currentMode = AtomicReference(Mode.NONE)
internal var currentMode: Mode
    get() = _currentMode.get()
    private set(m) = _currentMode.set(m)

private var _remote: Boolean = false
internal val isRemote: Boolean
    get() = _remote

internal val i2cMultiplexer: I2CMultiplexer by lazy { I2CMultiplexer() }

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
        AppCommon.executor.scheduleAtFixedRate(
            ::modeAndKeyboardCheck,
            100,
            100,
            java.util.concurrent.TimeUnit.MILLISECONDS
        )
        AppCommon.awaitTermination()

        BackBenchPicker.keyHandler.buttonColors = listOf(Color.RED, Color.RED, Color.RED, Color.RED)
        logger.warn("Exiting ")

        EnvironmentDisplay.stop()
        TheStrip.stop()
        FrontBenchPicker.stop()
        BackBenchPicker.stop()
    }

    AppCommon.executor.shutdownNow()
    exitProcess(0)
}

private fun modeAndKeyboardCheck() {
    try {
        // adjust per time of day
        val hour = LocalTime.now().hour
        val mode = when {
            hour in (0..6) -> Mode.NIGHT
            hour <= 8 -> Mode.MORNING
            hour <= 20 -> Mode.DAYTIME
            else -> Mode.EVENING
        }
        // mode change or error cleared
        if (mode != currentMode) {
            BackBenchPicker.selectMenu(mode)
            mode.brightness.let {
                FrontBenchPicker.keyHandler.brightness = it
                BackBenchPicker.keyHandler.brightness = it
            }
            BackBenchPicker.currentMenu.displayMenu()
            FrontBenchPicker.currentMenu.displayMenu()
            currentMode = mode
        }

        // *****************************
        // ***** READ BUTTONS HERE *****
        // *****************************
        if (!BackBenchPicker.currentMenu.firstButton()) FrontBenchPicker.currentMenu.firstButton()
    } catch (e: Throwable) {
        logger.error("Error found - continuing", e)
    }
}
