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

import crackers.hassk.EntityState
import crackers.kobots.app.AppCommon
import crackers.kobots.buttonboard.environment.EnvironmentDisplay.scaleImageAt
import crackers.kobots.buttonboard.environment.EnvironmentDisplay.temperatureColor
import crackers.kobots.parts.loadImage
import org.json.JSONObject
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D

internal const val TEMP_HEIGHT = 40
internal const val TEMP_WIDTH = 128

/**
 * Show the outside temperature and weather icon. This is currently scaled to fit in a 40x128 pixel area.
 */
class OutsideState(val graphics2D: Graphics2D, val x: Int, val y: Int) {
    private val theFont = Font(Font.SANS_SERIF, Font.PLAIN, 32)
    private val theFM: FontMetrics

    init {
        theFM = graphics2D.getFontMetrics(theFont)
    }

    internal fun show() = with(graphics2D) {
        val outsideTemp = AppCommon.hasskClient.getState("weather.home")
        val temp = outsideTemp.temperature()

        // clear the top area
        color = Color.BLACK
        fillRect(x, y, TEMP_WIDTH, TEMP_HEIGHT)

        scaleImageAt(outsideTemp.icon()!!, x, y, TEMP_HEIGHT)
        font = theFont
        color = temperatureColor(temp)
        drawString("$temp\u2109", x + 50, theFM.ascent)
    }

    private fun EntityState.icon() = images[state] ?: images["default"].also {
        EnvironmentDisplay.logger.warn("Unknown weather state: $state")
    }

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
            "default" to loadImage("/screaming.png"),
        )
    }

    private fun EntityState.temperature() =
        try {
            JSONObject(attributes).optInt("temperature", 0)
        } catch (_: Exception) {
            0
        }
}
