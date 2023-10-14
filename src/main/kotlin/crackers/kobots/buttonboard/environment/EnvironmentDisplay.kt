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

package crackers.kobots.buttonboard.environment

import crackers.kobots.app.AppCommon
import crackers.kobots.devices.display.SSD1327
import crackers.kobots.parts.center
import crackers.kobots.parts.scheduleAtFixedRate
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.Future

/**
 * Weather and agenda display.
 */
object EnvironmentDisplay {
    val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var future: Future<*>

    private val screen by lazy {
        SSD1327().apply {
            displayOn = false
            clear()
        }
    }
    private val screenGraphics: Graphics2D
    private val image: BufferedImage

    internal val dateFont = Font(Font.SANS_SERIF, Font.BOLD, 14)
    internal val dateFontMetrics: FontMetrics

    internal const val MAX_W = 128
    internal const val MAX_H = 128

    init {
        image = BufferedImage(MAX_W, MAX_H, BufferedImage.TYPE_BYTE_GRAY).also { img: BufferedImage ->
            screenGraphics = (img.graphics as Graphics2D).also {
                dateFontMetrics = it.getFontMetrics(dateFont)
            }
        }
    }

    private val dateBottom = TEMP_HEIGHT + dateFontMetrics.height
    private val insideStuff = InsideTemps(screenGraphics, dateBottom + 10, MAX_W, MAX_H)
    private val outsideState = OutsideState(screenGraphics, 0, 0)

    fun start() {
        future = AppCommon.executor.scheduleAtFixedRate(::updateDisplay, Duration.ofSeconds(1), Duration.ofMinutes(5))
    }

    fun stop() {
        future.cancel(false)
        try {
            screen.displayOn = false
            screen.close()
        } catch (_: Exception) {
        }
    }

    fun updateDisplay() {
        try {
            // leave it off at night
            val localNow = LocalDateTime.now()
            if (localNow.hour >= 23 || localNow.hour <= 6) {
                screen.displayOn = false
                return
            }
            if (!screen.displayOn) {
                screen.displayOn = true
                // assuming this happens once a day, update the date
                showDate(localNow)
            }
            outsideState.show()
            insideStuff.show()

            with(screen) {
                display(image)
                show()
            }
        } catch (t: Throwable) {
            LoggerFactory.getLogger(this::class.java).error("Unable to display", t)
        }
    }

    /**
     * Shows the date in the top line for the agenda block.
     */
    private fun showDate(now: LocalDateTime) = with(screenGraphics) {
        color = Color.BLACK
        fillRect(0, 0, MAX_W, MAX_H)

        color = Color.WHITE
        font = dateFont
        val date = "${now.dayOfWeek.name.substring(0, 3)}  ${now.month.name.substring(0, 3)} ${now.dayOfMonth}"
        val x = dateFontMetrics.center(date, MAX_W)
        drawString(date, x, dateBottom)
    }

    internal fun Graphics2D.scaleImageAt(image: BufferedImage, x: Int, y: Int, width: Int, height: Int = width) {
        drawImage(image, x, y, width, height, null)
    }

    internal fun temperatureColor(temp: Int): Color = when {
        temp > 79 -> Color.YELLOW
        temp > 50 -> Color.GREEN
        temp > 40 -> Color.CYAN
        else -> Color.RED
    }
}
