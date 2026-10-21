/*
 * Copyright 2022-2026 by E. A. Graham, Jr.
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
import crackers.kobots.app.AppCommon.whileRunning
import crackers.kobots.coroutine.AppScope
import crackers.kobots.devices.getOrCreateI2CDevice
import crackers.kobots.devices.lighting.NeoPixel
import crackers.kobots.devices.lighting.WS2811.PixelColor
import crackers.kobots.devices.microcontroller.AdafruitSeeSaw
import crackers.kobots.parts.GOLDENROD
import crackers.kobots.parts.PURPLE
import crackers.kobots.parts.colorIntervalFromHSB
import crackers.kobots.parts.sleep
import kotlinx.coroutines.Job
import org.slf4j.LoggerFactory
import java.awt.Color
import java.time.LocalDateTime
import java.time.Month
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Run pretty stuff on the Neopixel strip.
 */
object TheStrip : AppCommon.Startable {
    private val logger = LoggerFactory.getLogger("TheStrip")
    private lateinit var seeSaw: AdafruitSeeSaw
    private lateinit var strip: NeoPixel

    private var lastMode: Mode? = null

    private lateinit var stripJob: Job

    private const val STRIP_SIZE = 30

    override fun start() {
        if (isRemote) return

        // because this can conflict with a thing on the multiplexer
        val i2cDevice = getOrCreateI2CDevice(1, 0x60)
        seeSaw = AdafruitSeeSaw(i2cDevice)
        strip =
            NeoPixel(seeSaw, STRIP_SIZE, 15).apply {
                brightness = 0.1f
                autoWrite = true
            }

        stripJob = AppScope.scheduleWithFixedDelay(10.seconds, 15.seconds, ::showIt)
        logger.warn("Strip started")
    }

    fun showIt() {
        whileRunning {
            // do this **all** the time if it's daytime
            if (currentMode.isDaytime()) specialDaytimeEffect()

            // so we only do this once
            if (currentMode != lastMode) {
                lastMode = currentMode
                when (currentMode) {
                    Mode.MORNING -> strip[0, STRIP_SIZE - 1] = PixelColor(GOLDENROD, brightness = 0.03f)
                    Mode.EVENING -> strip[0, STRIP_SIZE - 1] = PixelColor(Color.RED, brightness = 0.03f)
                    Mode.NIGHT -> strip[0, STRIP_SIZE - 1] = Color.BLACK
                    Mode.DAYTIME -> logger.debug("No-op")
                    Mode.DISABLED -> strip[0, STRIP_SIZE - 1] = PixelColor(Color.BLUE.darker(), brightness = 0.03f)
                    else -> strip[0, STRIP_SIZE - 1] = PixelColor(PURPLE, brightness = .7f)
                }
            }
        }
    }

    override fun stop() {
        if (isRemote) return

        if (::stripJob.isInitialized) {
            stripJob.cancel()
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
    private val rainbowColors = colorIntervalFromHSB(0f, 359f, STRIP_SIZE).map { PixelColor(it, brightness = .6f) }
    private val xmasColors =
        List(STRIP_SIZE) { index -> if (index % 2 == 0) Color.RED else Color.GREEN }
            .map { PixelColor(it, brightness = .3f) }
    private val caseyColors =
        List(STRIP_SIZE) { index ->
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
        for (count in 0 until STRIP_SIZE) {
            AppCommon.applicationRunning || return
            strip[count] = whichColors[lastIndexUsed++]
            if (lastIndexUsed >= 30) lastIndexUsed = 0
            150.milliseconds.sleep()
        }
        lastIndexUsed++
        if (lastIndexUsed >= 30) lastIndexUsed = 0
    }
}
