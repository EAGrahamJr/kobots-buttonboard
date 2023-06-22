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
import crackers.kobots.devices.lighting.NeoKey
import crackers.kobots.utilities.KobotSleep
import org.slf4j.LoggerFactory
import java.awt.Color
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.exitProcess

private val keyboard by lazy {
    NeoKey().apply { pixels.brightness = 0.05f }
}

// TODO temporary while testing
const val REMOTE_PI = "diozero.remote.hostname"
const val USELESS = "useless.local"

private val logger = LoggerFactory.getLogger("ButtonBox")

internal val runFlag = AtomicBoolean(true)

internal val currentMode = AtomicReference(Mode.NIGHT)

/**
 * Uses NeoKey 1x4 as a HomeAssistant controller (and likely other things).
 */
fun main() {
    val isRemote = false
//    System.setProperty(REMOTE_PI, USELESS)

    keyboard[3] = Color.RED
    var lastButtonsRead: List<Boolean> = listOf(false, false, false, false)

    if (!isRemote) TheStrip.start()
    while (runFlag.get()) {
        try {
            // adjust per time of day
            val hour = LocalTime.now().hour
            val mode = when {
                hour <= 6 -> Mode.NIGHT
                hour <= 8 -> Mode.MORNING
                hour <= 20 -> Mode.DAYTIME
                else -> Mode.EVENING
            }
            if (mode != currentMode.getAndSet(mode)) {
                keyboard.brightness = mode.brightness
                showIcons(mode)
                mode.colors.forEachIndexed { index, color ->
                    keyboard[index] = color
                }
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
                runFlag.set(false)
            } else {
                whichButtonsPressed.firstOrNull()?.let { button ->
                    TheActions.doStuff(button, mode)
                    keyboard[button] = mode.colors[button]
                }
            }
        } catch (e: Throwable) {
            logger.error("Error found - continuing", e)
        }
        KobotSleep.millis(50)
    }
    keyboard.fill(Color.RED)
    logger.warn("Exiting ")
    if (!isRemote) TheStrip.stop()
    TheScreen.close()
    keyboard.close()
    exitProcess(0)
}
