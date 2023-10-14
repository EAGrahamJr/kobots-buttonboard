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
import org.json.JSONObject
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import kotlin.math.roundToInt

class InsideTemps(val graphics: Graphics2D, val startDrawingAt: Int, val maxWidth: Int, val maxHeight: Int) {
    private val font = Font(Font.SANS_SERIF, Font.PLAIN, 12)
    private val fontMetrics: FontMetrics
    private val lineHeight: Int

    init {
        fontMetrics = graphics.getFontMetrics(font)
        lineHeight = fontMetrics.height
    }

    private val insideSensors = listOf(
        "sensor.cube_air_temperature",
        "sensor.trisensor_air_temperature",
        "sensor.bedroom_temperature",
    )

    /**
     * Show the inside temperatures.
     */
    internal fun show() = with(graphics) {
        // clear the bottom area
        color = Color.BLACK
        fillRect(
            0,
            startDrawingAt,
            maxWidth,
            maxHeight - startDrawingAt,
        )

        font = font

        insideSensors.forEachIndexed { index, sensor ->
            val y =
                startDrawingAt + (lineHeight * index) + fontMetrics.ascent

            val state = AppCommon.hasskClient.getState(sensor)
            val temp = try {
                state.state.toFloat().roundToInt()
            } catch (_: Exception) {
                0
            }

            color = Color.WHITE
            val name = JSONObject(state.attributes!!).getString("friendly_name")?.removeSuffix(" Temperature")
                ?: state.entityId.removeSuffix("_temperature")
            drawString(name, 0, y)

            color = EnvironmentDisplay.temperatureColor(temp)
            val tempText = "$temp\u2109"
            val left = fontMetrics.stringWidth(tempText) + 2
            drawString(tempText, maxWidth - left, y)
        }
    }
}
