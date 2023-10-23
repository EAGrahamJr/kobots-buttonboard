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

import com.diozero.api.I2CDevice
import crackers.kobots.app.AppCommon
import crackers.kobots.buttonboard.TheActions.mqttClient
import crackers.kobots.devices.lighting.NeoPixel
import crackers.kobots.devices.microcontroller.AdafruitSeeSaw
import crackers.kobots.parts.GOLDENROD
import crackers.kobots.parts.PURPLE
import crackers.kobots.parts.app.KobotSleep
import crackers.kobots.parts.colorIntervalFromHSB
import crackers.kobots.parts.scheduleWithFixedDelay
import org.slf4j.LoggerFactory
import java.awt.Color
import java.util.concurrent.Future
import kotlin.time.Duration.Companion.seconds

/**
 * Run pretty stuff on the Neopixel strip.
 */
object TheStrip {
    private val logger = LoggerFactory.getLogger("TheStrip")
    private lateinit var seeSaw: AdafruitSeeSaw
    private lateinit var strip: NeoPixel

    // run it all the way 'round
    private val rainbowColors = colorIntervalFromHSB(0f, 359f, 30)
    private var lastRainbowColorIndex: Int = 0

    private var lastMode: Mode? = null

    private lateinit var future: Future<*>

    private val stripOffset = 8
    private val stripLast = stripOffset + 29

    fun start() {
        if (isRemote) return

        seeSaw = AdafruitSeeSaw(I2CDevice(1, 0x60))
        strip = NeoPixel(seeSaw, 38, 15).apply {
            brightness = 0.1f
            autoWrite = true
        }
        future = AppCommon.executor.scheduleWithFixedDelay(10.seconds, 10.seconds, ::showIt)
        RosetteStatus.manageAliveChecks(strip, mqttClient, 0)
    }

    fun showIt() {
        try {
            if (currentMode == Mode.DAYTIME) showRainbow()
            if (currentMode == lastMode) return

            lastMode = currentMode
            when (currentMode) {
                Mode.NIGHT -> {
                    strip[stripOffset, stripLast] = Color.BLACK
                }

                Mode.MORNING -> {
                    strip.brightness = 0.03f
                    strip[stripOffset, stripLast] = GOLDENROD
                }

                Mode.DAYTIME -> {
                    strip.brightness = 0.4f
                }

                Mode.EVENING -> {
                    strip.brightness = 0.03f
                    strip[stripOffset, stripLast] = Color.RED
                }

                Mode.NONE -> {
                    strip.brightness = 0.01f
                    strip[stripOffset, stripLast] = PURPLE
                }
            }

            RosetteStatus.goToSleep.set(currentMode == Mode.NIGHT)
        } catch (e: Exception) {
            logger.error("Cannot display strip", e)
        }
    }

    fun stop() {
        if (isRemote) return

        if (::future.isInitialized) {
            future.cancel(true)
        }
        try {
            strip.fill(Color.BLACK)
            strip.show()
            seeSaw.close()
        } catch (e: Exception) {
            logger.error("Cannot close strip", e)
        }
    }

    private fun showRainbow() {
        for (count in stripOffset..stripLast) {
            AppCommon.applicationRunning || return
            strip[count] = rainbowColors[lastRainbowColorIndex++]
            if (lastRainbowColorIndex >= 30) lastRainbowColorIndex = 0
            KobotSleep.millis(75)
        }
        lastRainbowColorIndex++
        if (lastRainbowColorIndex >= 30) lastRainbowColorIndex = 0
    }
}
