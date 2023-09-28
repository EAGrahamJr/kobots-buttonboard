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

import crackers.kobots.buttonboard.TheActions.Actions
import crackers.kobots.parts.GOLDENROD
import crackers.kobots.parts.PURPLE
import java.awt.Color

/**
 * Shared stuff
 * @Deprecated("Use ModeMenus")
 */
internal enum class Mode(
    val actions: List<Actions>,
    val images: List<Images>,
    val colors: List<Color>,
    val text: List<String>,
    val brightness: Float = 0.1f
) {
    NIGHT(
        listOf(Actions.NOT_ALL, Actions.TOP, Actions.MORNING, Actions.BEDROOM),
        listOf(Images.EXIT, Images.LIGHTBULB, Images.SUN, Images.BED),
        listOf(Color.DARK_GRAY, Color.GREEN, GOLDENROD, Color.PINK),
        listOf("Off", "Top", "Morn", "Bed"),
        .01f
    ),
    MORNING(
        listOf(Actions.MORNING, Actions.TOP, Actions.KITCHEN, Actions.OFFICE_FAN),
        listOf(Images.SUN, Images.LIGHTBULB, Images.RESTAURANT, Images.FAN),
        listOf(GOLDENROD, Color.GREEN, Color.CYAN, Color.BLUE),
        listOf("Morn", "Top", "Kit", "Fan"),
        .05f
    ),
    DAYTIME(
        listOf(Actions.TOP, Actions.TV, Actions.MOVIE, Actions.OFFICE_FAN),
        listOf(Images.LIGHTBULB, Images.TV, Images.MOVIE, Images.FAN),
        listOf(Color.GREEN, PURPLE, Color.RED, Color.BLUE),
        listOf("Top", "TV", "Movie", "Fan")
    ),
    EVENING(
        listOf(Actions.BEDTIME, Actions.LATE_NIGHT, Actions.NOT_ALL, Actions.OFFICE_FAN),
        listOf(Images.BED, Images.MOON, Images.EXIT, Images.FAN),
        listOf(Color.PINK, Color.RED, Color.DARK_GRAY, Color.BLUE),
        listOf("Bed", "Late", "Off", "Fan"),
        .05f
    )
}
