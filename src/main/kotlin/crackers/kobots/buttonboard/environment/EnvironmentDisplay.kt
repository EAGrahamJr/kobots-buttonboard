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

package crackers.kobots.buttonboard.environment

import com.diozero.devices.oled.SH1106
import com.diozero.devices.oled.SsdOledCommunicationChannel
import crackers.kobots.app.AppCommon
import crackers.kobots.app.AppCommon.ignoreErrors
import crackers.kobots.app.AppCommon.whileRunning
import crackers.kobots.buttonboard.currentMode
import crackers.kobots.buttonboard.i2cMultiplexer
import crackers.kobots.graphics.animation.MatrixRain
import crackers.kobots.graphics.center
import crackers.kobots.parts.scheduleAtFixedRate
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.time.LocalDateTime
import java.util.concurrent.Future
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Weather and agenda display.
 */
object EnvironmentDisplay : AppCommon.Startable {
    val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var future: Future<*>

    private lateinit var screen: SH1106
    private val screenGraphics: Graphics2D
    private val image: BufferedImage

    internal val dateFont = Font(Font.SANS_SERIF, Font.BOLD, 16)
    internal val dateFontMetrics: FontMetrics

    internal const val MAX_W = 128
    internal const val MAX_H = 64

    private val insideStuff: InsideTemps
    private val outsideState: OutsideState
    private val dateBottom: Int
    private val outsideTop: Int

    init {
        image =
            BufferedImage(MAX_W, MAX_H, BufferedImage.TYPE_BYTE_GRAY).also { img: BufferedImage ->
                screenGraphics =
                    (img.graphics as Graphics2D).also {
                        dateFontMetrics = it.getFontMetrics(dateFont)
                        it.background = Color.BLACK
                    }
            }
        insideStuff = InsideTemps(screenGraphics, MAX_W)
        outsideState = OutsideState(screenGraphics, MAX_W)
        outsideTop = MAX_H - (outsideState.tempHeight + 1)
        dateBottom = MAX_H
    }

    // @formatter:off
    private val rain =
        MatrixRain(
            screenGraphics,
            0,
            0,
            MAX_W,
            MAX_H,
            displayFont = Font(Font.MONOSPACED, Font.PLAIN, 8),
            useBold = false,
            updateSpeed = 20.milliseconds,
        )
    // @formatter:on

    override fun start() {
        val block = {
            val i2cDevice = i2cMultiplexer.getI2CDevice(7, SH1106.DEFAULT_I2C_ADDRESS)
            screen =
                SH1106(SsdOledCommunicationChannel.I2cCommunicationChannel(i2cDevice)).apply {
                    display = false
                    clear()
                    setContrast(0x20.toByte())
                }

            future = AppCommon.executor.scheduleAtFixedRate(15.seconds, 5.minutes, ::updateDisplay)
        }
        ignoreErrors(block, true)
    }

    override fun stop() {
        if (::future.isInitialized) future.cancel(true)
        ignoreErrors({
                         screen.display = false
                         screen.close()
                     })
    }

    private var lastGraphicShown = -1
    private const val RAIN = 0
    private const val INSIDE = 1
    private const val OUTSIDE = 2

    fun updateDisplay() {
        whileRunning {
            // leave it off at night
            if (currentMode.isNight()) {
                screen.display = false
            } else {
                if (!screen.display) screen.display = true
                val next = if (lastGraphicShown == INSIDE) OUTSIDE else INSIDE

                screenGraphics.clearRect(0, 0, MAX_W, MAX_H)
                when (next) {
                    RAIN ->
                        rain.start {
                            ignoreErrors(::updateScreen)
                        }

                    OUTSIDE -> {
                        outsideState.show()
                        showDate()
                        updateScreen()
                    }

                    else -> {
                        insideStuff.show()
                        updateScreen()
                    }
                }
                logger.debug("Switched to $next")
                lastGraphicShown = next
            }
        }
    }

    private fun updateScreen() =
        with(screen) {
            display(image)
            show()
        }

    /**
     * Shows the date in the top line for the agenda block.
     */
    private fun showDate() =
        with(screenGraphics) {
            val now = LocalDateTime.now()
            color = Color.WHITE
            font = dateFont
            val date = "${now.dayOfWeek.name.substring(0, 3)}  ${now.month.name.substring(0, 3)} ${now.dayOfMonth}"
            val x = dateFontMetrics.center(date, MAX_W)
            drawString(date, x, dateBottom)
        }

    internal fun Graphics2D.scaleImageAt(
        image: BufferedImage,
        x: Int,
        y: Int,
        width: Int,
        height: Int = width,
    ) {
        drawImage(image, x, y, width, height, null)
    }

    internal fun temperatureColor(temp: Int): Color =
        when {
            temp > 79 -> Color.YELLOW
            temp > 50 -> Color.GREEN
            temp > 40 -> Color.CYAN
            else -> Color.RED
        }
}
