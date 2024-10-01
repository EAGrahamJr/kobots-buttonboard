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
import crackers.kobots.app.AppCommon.mqttClient
import crackers.kobots.app.AppCommon.whileRunning
import crackers.kobots.devices.getOrCreateI2CDevice
import crackers.kobots.devices.lighting.NeoPixel
import crackers.kobots.devices.lighting.WS2811.PixelColor
import crackers.kobots.devices.microcontroller.AdafruitSeeSaw
import crackers.kobots.parts.GOLDENROD
import crackers.kobots.parts.PURPLE
import crackers.kobots.parts.app.KobotSleep
import crackers.kobots.parts.colorIntervalFromHSB
import crackers.kobots.parts.scheduleWithDelay
import org.slf4j.LoggerFactory
import java.awt.Color
import java.time.LocalDateTime
import java.time.Month
import java.util.concurrent.Future
import kotlin.time.Duration.Companion.seconds

/**
 * Run pretty stuff on the Neopixel strip.
 */
object TheStrip {
    private val logger = LoggerFactory.getLogger("TheStrip")
    private lateinit var seeSaw: AdafruitSeeSaw
    private lateinit var strip: NeoPixel

    private var lastMode: Mode? = null

    private lateinit var future: Future<*>

    private val stripOffset = 8
    private val stripLast = stripOffset + 29

    fun start() {
        if (isRemote) return

        // because this can conflict with a thing on the multiplexer
        val i2cDevice = getOrCreateI2CDevice(1, 0x60)
        seeSaw = AdafruitSeeSaw(i2cDevice)
        strip =
            NeoPixel(seeSaw, 38, 15).apply {
                brightness = 0.1f
                autoWrite = true
            }
        future = AppCommon.executor.scheduleWithDelay(10.seconds, ::showIt)
        RosetteStatus.manageAliveChecks(strip, mqttClient, 0)
    }

    fun showIt() {
        whileRunning {
            // do this **all** the time if it's daytime
            if (currentMode.isDaytime()) specialDaytimeEffect()

            // so we only do this once
            if (currentMode != lastMode) {
                lastMode = currentMode
                when (currentMode) {
                    Mode.MORNING -> strip[stripOffset, stripLast] = PixelColor(GOLDENROD, brightness = 0.03f)
                    Mode.EVENING -> strip[stripOffset, stripLast] = PixelColor(Color.RED, brightness = 0.03f)
                    Mode.NIGHT -> strip[stripOffset, stripLast] = Color.BLACK
                    Mode.DAYTIME -> { // handled by the daytime effect
                    }

                    Mode.DISABLED -> strip[stripOffset, stripLast] = PixelColor(Color.BLUE.darker(), brightness = 0.03f)
                    else -> strip[stripOffset, stripLast] = PixelColor(PURPLE, brightness = .7f)
                }

                RosetteStatus.goToSleep.set(currentMode == Mode.NIGHT || currentMode == Mode.DISABLED)
            }
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

    // run it all the way 'round
    private val rainbowColors = colorIntervalFromHSB(0f, 359f, 30).map { PixelColor(it, brightness = .6f) }
    private val xmasColors =
        List(30) { index -> if (index % 2 == 0) Color.RED else Color.GREEN }
            .map { PixelColor(it, brightness = .3f) }
    private val caseyColors =
        List(30) { index ->
            when (index % 3) {
                0 -> Color.GRAY
                1 -> PURPLE
                else -> Color.BLACK
            }
        }.map { PixelColor(it, brightness = .5f) }

    private var lastIndexUsed: Int = 0

    private val whichColors: List<PixelColor>
        get() {
            val now = LocalDateTime.now()
            return when {
                // casey's birthday
                now.month == Month.DECEMBER && now.dayOfMonth == 31 -> caseyColors
                // xmas day just because
                now.month == Month.DECEMBER && now.dayOfMonth == 25 -> xmasColors
                // otherwise rainbow
                else -> rainbowColors
            }
        }

    private fun specialDaytimeEffect() {
        for (count in stripOffset..stripLast) {
            AppCommon.applicationRunning || return
            strip[count] = whichColors[lastIndexUsed++]
            if (lastIndexUsed >= 30) lastIndexUsed = 0
            KobotSleep.millis(75)
        }
        lastIndexUsed++
        if (lastIndexUsed >= 30) lastIndexUsed = 0
    }
}
