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

package crackers.kobots.buttonboard.environment

import com.diozero.api.I2CDevice
import com.diozero.devices.oled.SsdOledCommunicationChannel
import crackers.kobots.app.AppCommon
import crackers.kobots.app.AppCommon.ignoreErrors
import crackers.kobots.app.AppCommon.whileRunning
import crackers.kobots.buttonboard.currentMode
import crackers.kobots.devices.display.SSD1327
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
object EnvironmentDisplay {
    val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var future: Future<*>

    private lateinit var screen: SSD1327
    private val screenGraphics: Graphics2D
    private val image: BufferedImage

    internal val dateFont = Font(Font.SANS_SERIF, Font.BOLD, 16)
    internal val dateFontMetrics: FontMetrics

    internal const val MAX_W = 128
    internal const val MAX_H = 128

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
        dateBottom = (insideStuff.tempHeight + outsideTop + dateFontMetrics.height) / 2
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
            updateSpeed = 10.milliseconds,
        )
    // @formatter:on

    fun start() {
        val block = {
            val i2cDevice = I2CDevice(1, SSD1327.QWIIC_I2C_ADDRESS)
            screen = SSD1327(SsdOledCommunicationChannel.I2cCommunicationChannel(i2cDevice))

            screen.displayOn = false
            screen.clear()

            future = AppCommon.executor.scheduleAtFixedRate(15.seconds, 5.minutes, ::updateDisplay)
        }
        ignoreErrors(block, true)
    }

    fun stop() {
        if (::future.isInitialized) future.cancel(true)
        ignoreErrors({
                         screen.displayOn = false
                         screen.close()
                     })
    }

    private var lastEnvironmentUpdate = false

    fun updateDisplay() {
        whileRunning {
            // leave it off at night
            if (currentMode.isNight()) {
                screen.displayOn = false
            } else {
                if (!screen.displayOn) screen.displayOn = true
                if (!lastEnvironmentUpdate) {
                    rain.stop()
                    screenGraphics.clearRect(0, 0, MAX_W, MAX_H)
                    showDate()
                    outsideState.show(y = outsideTop)
                    insideStuff.show()
                    lastEnvironmentUpdate = true
                    updateScreen()
                } else {
                    rain.start {
                        lastEnvironmentUpdate = false
                        updateScreen()
                    }
                }
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
