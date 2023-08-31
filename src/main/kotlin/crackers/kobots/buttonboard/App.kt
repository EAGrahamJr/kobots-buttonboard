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

import crackers.kobots.buttonboard.TheScreen.showIcons
import crackers.kobots.devices.io.NeoKey
import crackers.kobots.mqtt.KobotsMQTT
import crackers.kobots.utilities.KobotSleep
import org.slf4j.LoggerFactory
import java.awt.Color
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.exitProcess

const val REMOTE_PI = "diozero.remote.hostname"

private val logger = LoggerFactory.getLogger("ButtonBox")

// set up an executor that everyone can share
internal val executor = Executors.newScheduledThreadPool(5)

private val _runFlag = AtomicBoolean(true)
internal var runFlag: Boolean
    get() = _runFlag.get()
    private set(b) = _runFlag.set(b)

private val _currentMode = AtomicReference(Mode.NIGHT)
internal var currentMode: Mode
    get() = _currentMode.get()
    private set(m) = _currentMode.set(m)

private var _remote: Boolean = false
internal val isRemote: Boolean
    get() = _remote

/**
 * Uses NeoKey 1x4 as a HomeAssistant controller (and likely other things).
 */
fun main(args: Array<String>) {
    _remote = if (args.isNotEmpty()) {
        System.setProperty(REMOTE_PI, args[0])
        true
    } else {
        false
    }

    val keyboard = NeoKey().apply { pixels.brightness = 0.05f }
    val client = KobotsMQTT("useless", "tcp://192.168.1.4:1883").apply {
        startAliveCheck()
    }

    keyboard[3] = Color.RED
    var lastButtonsRead: List<Boolean> = listOf(false, false, false, false)

    TheStrip.start()
    EnvironmentDisplay.start()

    while (runFlag) {
        try {
            // adjust per time of day
            val hour = LocalTime.now().hour
            val mode = when {
                hour in (1..6) -> Mode.NIGHT
                hour <= 8 -> Mode.MORNING
                hour <= 20 -> Mode.DAYTIME
                else -> Mode.EVENING
            }
            // mode change or error cleared
            if (mode != currentMode) {
                keyboard.brightness = mode.brightness
                showIcons(mode)
                mode.colors.forEachIndexed { index, color ->
                    keyboard[index] = color
                }
                currentMode = mode
            }

            /*
             * This is purely button driven, so use the buttons - try to "debounce" by only detecting changes between
             * iterations. This is because humans are slow
             */
            val currentButtons = keyboard.read()
            val whichButtonsPressed = if (currentButtons != lastButtonsRead) {
                lastButtonsRead = currentButtons
                currentButtons.mapIndexedNotNull { index, b ->
                    if (b) {
                        keyboard[index] = Color.YELLOW
                        index
                    } else {
                        null
                    }
                }
            } else {
                emptyList()
            }
            if (whichButtonsPressed.size > 1) {
                runFlag = false
            } else {
                whichButtonsPressed.firstOrNull()?.let { button ->
                    TheActions.doStuff(button, currentMode)
                    keyboard[button] = currentMode.colors[button]
                }
            }
        } catch (e: Throwable) {
            logger.error("Error found - continuing", e)
        }
        KobotSleep.millis(100)
    }
    keyboard.fill(Color.RED)
    logger.warn("Exiting ")

//    client.close()
    EnvironmentDisplay.stop()
    if (!isRemote) TheStrip.stop()
    TheScreen.close()
    keyboard.close()
    executor.shutdownNow()
    exitProcess(0)
}
