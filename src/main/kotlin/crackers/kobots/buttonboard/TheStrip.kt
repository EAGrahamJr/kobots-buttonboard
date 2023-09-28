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
import com.diozero.util.SleepUtil
import crackers.kobots.app.AppCommon
import crackers.kobots.buttonboard.TheActions.mqttClient
import crackers.kobots.devices.lighting.NeoPixel
import crackers.kobots.devices.microcontroller.AdafruitSeeSaw
import crackers.kobots.parts.GOLDENROD
import crackers.kobots.parts.PURPLE
import crackers.kobots.parts.colorIntervalFromHSB
import org.slf4j.LoggerFactory
import java.awt.Color
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * Run pretty stuff on the Neopixel strip.
 */
object TheStrip : Runnable {
    private val logger = LoggerFactory.getLogger("TheStrip")
    private lateinit var seeSaw: AdafruitSeeSaw
    private lateinit var strip: NeoPixel

    // 0 and 360 are the same, so back off (and the 30 makes this even easier)
    private val rainbowColors = colorIntervalFromHSB(0f, 348f, 30)
    private var lastRainbowColorIndex: Int = 0

    private var lastMode: Mode? = null

    private lateinit var future: Future<*>

    private val stripOffset = 8
    private val stripLast = stripOffset + 29

    fun start(): Boolean {
        if (isRemote) return true

        for (i in 0 until 10) try {
            seeSaw = AdafruitSeeSaw(I2CDevice(1, 0x60))
            strip = NeoPixel(seeSaw, 38, 15)
            logger.warn("Took $i tries to initialize")

            strip.brightness = 0.1f
            strip.autoWrite = true

            future = AppCommon.executor.scheduleAtFixedRate(this, 10, 10, TimeUnit.SECONDS)
            RosetteStatus.manageAliveChecks(strip, mqttClient, 0)
            return true
        } catch (t: Throwable) {
            SleepUtil.busySleep(50)
        }
        logger.error("Failed to initialize")
        return false
    }

    override fun run() {
        try {
            if (currentMode == Mode.DAYTIME) showRainbow()
            if (currentMode == lastMode) return

            lastMode = currentMode
            strip.autoWrite = when (currentMode) {
                Mode.NIGHT -> {
                    strip[stripOffset, stripLast] = Color.BLACK
                    true
                }

                Mode.MORNING -> {
                    strip.brightness = 0.03f
                    strip[stripOffset, stripLast] = GOLDENROD
                    true
                }

                Mode.DAYTIME -> {
                    strip.brightness = 0.2f
                    false
                }

                Mode.EVENING -> {
                    strip.brightness = 0.03f
                    strip[stripOffset, stripLast] = Color.RED
                    true
                }

                Mode.NONE -> {
                    strip.brightness = 0.01f
                    strip[stripOffset, stripLast] = PURPLE
                    true
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
            runFlag || return
            strip[count] = rainbowColors[lastRainbowColorIndex++]
            if (lastRainbowColorIndex >= 30) lastRainbowColorIndex = 0
        }
        strip.show()
        lastRainbowColorIndex++
        if (lastRainbowColorIndex >= 30) lastRainbowColorIndex = 0
    }
}
