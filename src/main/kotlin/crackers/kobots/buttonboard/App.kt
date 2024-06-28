/*
 * Copyright 2022-2024 by E. A. Graham, Jr.
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
import crackers.kobots.app.AppCommon.REMOTE_PI
import crackers.kobots.app.AppCommon.mqttClient
import crackers.kobots.app.AppCommon.whileRunning
import crackers.kobots.buttonboard.buttons.BackBenchPicker
import crackers.kobots.buttonboard.buttons.FrontBenchPicker
import crackers.kobots.buttonboard.environment.EnvironmentDisplay
import crackers.kobots.devices.expander.I2CMultiplexer
import crackers.kobots.mqtt.homeassistant.DeviceIdentifier
import crackers.kobots.parts.scheduleWithFixedDelay
import org.slf4j.LoggerFactory
import java.time.LocalTime
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Defines what various parts of the day are
 */
enum class Mode(val brightness: Float) {
    NONE(0f),
    NIGHT(.005f),
    MORNING(.02f),
    DAYTIME(0.05f),
    EVENING(.03f),
    AUDIO(0.05f),
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

            listOf(BackBenchPicker, FrontBenchPicker).forEach {
                it.selectMenu(m)
                it.keyHandler.brightness = m.brightness
                it.currentMenu.displayMenu()
            }
        }
    }

private var runningRemote: Boolean = false
internal val isRemote: Boolean
    get() = runningRemote

internal val i2cMultiplexer: I2CMultiplexer by lazy { I2CMultiplexer() }
internal val haDevice = DeviceIdentifier("Kobots", "ButtonBoard")

private lateinit var theFuture: ScheduledFuture<*>

private val shutDown = AtomicBoolean(false)

/**
 * Uses NeoKey 1x4 as a HomeAssistant controller (and likely other things).
 */
fun main(args: Array<String>) {
    runningRemote = args.isNotEmpty().also { if (it) System.setProperty(REMOTE_PI, args[0]) }

    TheStrip.start()
    i2cMultiplexer.use {
        Rooty.start()
        EnvironmentDisplay.start()
        FrontBenchPicker.start()
        BackBenchPicker.start()

        // start the "main" loop -- note that the Java scheduler is more CPU efficient than simply looping and waiting
        theFuture = AppCommon.executor.scheduleWithFixedDelay(1.seconds, 50.milliseconds, ::modeAndKeyboardCheck)
        // start the MQTT client
        startMqttStuff()

        Runtime.getRuntime().addShutdownHook(
            thread(start = false) {
                logger.error("Terminating")
                shutdown()
            },
        )
        AppCommon.awaitTermination()
        logger.warn("Exiting ")
        shutdown()
    }

    AppCommon.executor.shutdownNow()
    exitProcess(0)
}

fun shutdown() {
    shutDown.compareAndSet(false, true) || return
    theFuture.cancel(true)

    AppCommon.applicationRunning = false
    Rooty.stop()
    FrontBenchPicker.stop()
    BackBenchPicker.stop()
    EnvironmentDisplay.stop()
    TheStrip.stop()
}

private fun startMqttStuff() =
    with(mqttClient) {
        startAliveCheck()
        allowEmergencyStop()

//        subscribeJSON(KobotsMQTT.KOBOTS_EVENTS) { payload ->
//            with(payload) {
//                logger.info("Kobots event: {}", payload)
//                 if the "arm" thingies completes an eye-drop, start blinking the return button
//                if (optString("source") == "TheArm" && optString("sequence") == "LocationPickup" && optBoolean("started")) {
//                    FrontBenchPicker.selectMenu(FrontBenchActions.STANDARD_ROBOT)
//                    // TODO "zero" the rotor?
//                    FrontBenchPicker.startBlinky()
//                }
//            }
//        }
        subscribeJSON("kobots_auto/caseys_lamp/state") { payload ->
            if (currentMode == Mode.MORNING && payload.optString("state", "off") == "on") currentMode = Mode.DAYTIME
        }
    }

private fun modeAndKeyboardCheck() {
    whileRunning {
        // adjust per time of day
        val hour = LocalTime.now().hour
        currentMode =
            when {
                hour in (0..6) -> Mode.NIGHT
                hour <= 8 && currentMode != Mode.DAYTIME -> Mode.MORNING
                hour <= 20 -> if (currentMode != Mode.AUDIO) Mode.DAYTIME else currentMode
                else -> if (currentMode != Mode.NIGHT) Mode.EVENING else currentMode
            }

        // *****************************
        // ***** READ BUTTONS HERE *****
        // *****************************
        Rooty.clickOrTwist()
        (BackBenchPicker.currentMenu.firstButton() || FrontBenchPicker.currentMenu.firstButton())
    }
}
