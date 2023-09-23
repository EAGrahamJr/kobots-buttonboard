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

import crackers.hassk.EntityState
import crackers.kobots.buttonboard.TheActions.hasskClient
import crackers.kobots.devices.display.SSD1327
import crackers.kobots.utilities.center
import crackers.kobots.utilities.loadImage
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.time.LocalDateTime
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 * Weather and agenda display.
 */
object EnvironmentDisplay : Runnable {
    val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var future: Future<*>

    private val screen by lazy {
        SSD1327(SSD1327.ADAFRUIT_STEMMA).apply {
            displayOn = false
            clear()
        }
    }
    private val screenGraphics: Graphics2D
    private val image: BufferedImage

    private val tempFont = Font(Font.SANS_SERIF, Font.PLAIN, 32)
    private val tempFontMetrics: FontMetrics
    private val dateFont = Font(Font.SANS_SERIF, Font.BOLD, 14)
    private val dateFontMetrics: FontMetrics
    private val bottomFont = Font(Font.SANS_SERIF, Font.PLAIN, 12)
    private val bottomFontMetrics: FontMetrics
    private val bottomLineHeight: Int

    private const val MAX_W = 128
    private const val MAX_H = 128
    private const val TEMP_HEIGHT = 40

    init {
        image = BufferedImage(MAX_W, MAX_H, BufferedImage.TYPE_BYTE_GRAY).also { img: BufferedImage ->
            screenGraphics = (img.graphics as Graphics2D).also {
                tempFontMetrics = it.getFontMetrics(tempFont)
                bottomFontMetrics = it.getFontMetrics(bottomFont)
                bottomLineHeight = bottomFontMetrics.height + 1
                dateFontMetrics = it.getFontMetrics(dateFont)
            }
        }
    }

    private val dateBottom = TEMP_HEIGHT + dateFontMetrics.height
    private val bottomStartsAt = dateBottom + 10

    private val images by lazy {
        mapOf(
            "clear-night" to loadImage("/weather/clear-night.png"),
            "cloudy" to loadImage("/weather/cloudy.png"),
            "fog" to loadImage("/weather/fog.png"),
            "mixed" to loadImage("/weather/mixed.png"),
            "partlycloudy" to loadImage("/weather/partly-cloudy.png"),
            "rainy" to loadImage("/weather/rain.png"),
            "snow" to loadImage("/weather/snow.png"),
            "sunny" to loadImage("/weather/sunny.png"),
            "windy" to loadImage("/weather/windy.png"),
            "pouring" to loadImage("/weather/rainy_heavy.png"),
            "default" to loadImage("/screaming.png")
        )
    }

    private const val SLEEP_SECONDS = 600L

    fun start() {
        future = executor.scheduleAtFixedRate(this, 1, SLEEP_SECONDS, TimeUnit.SECONDS)
    }

    fun stop() {
        future.cancel(false)
        try {
            screen.displayOn = false
            screen.close()
        } catch (_: Exception) {
        }
    }

    override fun run() {
        try {
            // leave it off at night
            val localNow = LocalDateTime.now()
            if (localNow.hour >= 22 || localNow.hour <= 6) {
                screen.displayOn = false
                return
            }
            if (!screen.displayOn) {
                screen.displayOn = true
                // assuming this happens once a day, update the date
                screenGraphics.showDate(localNow)
            }
            screenGraphics.showOutside()
            screenGraphics.showInsideTemps()
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
    private fun Graphics2D.showDate(now: LocalDateTime) {
        color = Color.BLACK
        fillRect(0, 0, MAX_W, MAX_H)

        color = Color.WHITE
        font = dateFont
        val date = "${now.dayOfWeek.name.substring(0, 3)}  ${now.month.name.substring(0, 3)} ${now.dayOfMonth}"
        val x = dateFontMetrics.center(date, MAX_W)
        drawString(date, x, dateBottom)
    }

    /**
     * Show the outside temperature and weather icon.
     */
    private fun Graphics2D.showOutside() {
        val outsideTemp = hasskClient.getState("weather.home")
        val temp = outsideTemp.temperature()

        // clear the top area
        color = Color.BLACK
        fillRect(0, 0, MAX_W, TEMP_HEIGHT)

        scaleImageAt(outsideTemp.icon()!!, 0, 0, TEMP_HEIGHT)
        font = tempFont
        color = temperatureColor(temp)
        drawString("$temp\u2109", 50, tempFontMetrics.ascent)
    }

    private val insideSensors = listOf(
        "sensor.cube_air_temperature",
        "sensor.trisensor_air_temperature",
        "sensor.bedroom_temperature"
    )

    /**
     * Show the inside temperatures.
     */
    private fun Graphics2D.showInsideTemps() {
        // clear the bottom area
        color = Color.BLACK
        fillRect(0, bottomStartsAt, MAX_W, MAX_H - bottomStartsAt)

        font = bottomFont

        insideSensors.forEachIndexed { index, sensor ->
            val y = bottomStartsAt + (bottomLineHeight * index) + bottomFontMetrics.ascent

            val state = hasskClient.getState(sensor)
            val temp = try {
                state.state.toFloat().roundToInt()
            } catch (_: Exception) {
                0
            }

            color = Color.WHITE
            val name = JSONObject(state.attributes!!).getString("friendly_name")?.removeSuffix(" Temperature")
                ?: state.entityId.removeSuffix("_temperature")
            drawString(name, 0, y)

            color = temperatureColor(temp)
            val tempText = "$temp\u2109"
            val left = bottomFontMetrics.stringWidth(tempText) + 2
            drawString(tempText, MAX_W - left, y)
        }
    }

    private fun EntityState.temperature() =
        try {
            JSONObject(attributes).optInt("temperature", 0)
        } catch (_: Exception) {
            0
        }

    private fun EntityState.icon() = images[state] ?: images["default"].also {
        logger.warn("Unknown weather state: $state")
    }

    private fun Graphics2D.scaleImageAt(image: BufferedImage, x: Int, y: Int, width: Int, height: Int = width) {
        drawImage(image, x, y, width, height, null)
    }

    private fun temperatureColor(temp: Int): Color = when {
        temp > 79 -> Color.YELLOW
        temp > 50 -> Color.GREEN
        temp > 40 -> Color.CYAN
        else -> Color.RED
    }
}
