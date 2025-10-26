/*
 * Copyright 2022-2025 by E. A. Graham, Jr.
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
import crackers.kobots.app.AppCommon.ignoreErrors
import crackers.kobots.app.AppCommon.mqttClient
import crackers.kobots.app.AppCommon.whileRunning
import crackers.kobots.buttonboard.buttons.BackBenchPicker
import crackers.kobots.buttonboard.buttons.FrontBenchPicker
import crackers.kobots.buttonboard.environment.EnvironmentDisplay
import crackers.kobots.devices.expander.I2CMultiplexer
import crackers.kobots.mqtt.homeassistant.DeviceIdentifier
import crackers.kobots.mqtt.homeassistant.KobotSwitch
import crackers.kobots.parts.movement.async.AppScope
import kotlinx.coroutines.Job
import org.slf4j.LoggerFactory
import java.lang.Thread.sleep
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds

/**
 * Defines what various parts of the day are
 */
enum class Mode(
    val brightness: Float,
) {
    NONE(0f),
    NIGHT(.005f),
    MORNING(.02f),
    DAYTIME(0.05f),
    EVENING(.03f),
    AUDIO(0.05f),
    DISABLED(0.01f),
    ;

    fun isNight() = this == NIGHT || this == EVENING

    fun isDaytime() = this == DAYTIME
}

private val logger = LoggerFactory.getLogger("ButtonBox")

private val myCurrentMode = AtomicReference(Mode.NONE)
var currentMode: Mode
    get() = myCurrentMode.get()

    @Synchronized
    set(m) {
        if (myCurrentMode.get() != m) {
            myCurrentMode.set(m)

            listOf(BackBenchPicker, FrontBenchPicker).forEach {
                it.selectMenu(m)
            }
        }
    }

private var runningRemote: Boolean = false
internal val isRemote: Boolean
    get() = runningRemote

internal val i2cMultiplexer: I2CMultiplexer by lazy { I2CMultiplexer() }
internal val haDevice = DeviceIdentifier("Kobots", "ButtonBoard")

private lateinit var theFuture: Job

private val shutDown = AtomicBoolean(false)
private val buttonsEnabled = AtomicBoolean(true)

private val schtuff = listOf(TheStrip, EnvironmentDisplay, FrontBenchPicker, BackBenchPicker)

/**
 * Uses NeoKey 1x4 as a HomeAssistant controller (and likely other things).
 */
fun main(args: Array<String>) {
    runningRemote = args.isNotEmpty().also { if (it) System.setProperty(REMOTE_PI, args[0]) }

    schtuff.forEach(AppCommon.Startable::start)

    // anti-cat device: disable buttons (enabled by default)
    val switch =
        object : KobotSwitch.Companion.OnOffDevice {
            override var isOn: Boolean
                get() = buttonsEnabled.get()
                set(v) {
                    buttonsEnabled.set(v)
                }
            override val name = "Enable Buttons"
        }
    KobotSwitch(switch, "bb_enable", "Enable BB", haDevice).start()

    // start the "mode" loop
    theFuture = AppScope.scheduleWithFixedDelay(1.seconds, 15.seconds, ::modeAndKeyboardCheck)
    // start the MQTT client
    startMqttStuff()

    Runtime.getRuntime().addShutdownHook(thread(start = false, block = ::shutdown))
    AppCommon.awaitTermination()
    logger.warn("Exiting ")
    exitProcess(0)
}

fun shutdown() {
    if (!shutDown.compareAndSet(false, true)) {
        logger.error("Already stopped")
        return
    }
    theFuture.cancel()
    logger.error("Shutdown")

    schtuff.forEach { ignoreErrors(it::stop) }
    // because
    sleep(1000)
    i2cMultiplexer.close()
}

private fun startMqttStuff() =
    with(mqttClient) {
        startAliveCheck()
        subscribeJSON("kobots_auto/bedroom_lamp/state") { payload ->
            if (currentMode == Mode.MORNING && payload.optString("state", "off") == "on") currentMode = Mode.DAYTIME
        }
    }

private fun modeAndKeyboardCheck() {
    whileRunning {
        // adjust per time of day
        val hour = LocalTime.now().hour
        currentMode =
            when {
                !buttonsEnabled.get() -> Mode.DISABLED
                hour in (0..6) -> Mode.NIGHT
                hour <= 8 && currentMode != Mode.DAYTIME -> Mode.MORNING
                hour <= 20 -> if (currentMode != Mode.AUDIO) Mode.DAYTIME else currentMode
                else -> if (currentMode != Mode.NIGHT) Mode.EVENING else currentMode
            }
    }
}
