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

package crackers.kobots.buttonboard.buttons

import crackers.kobots.app.AppCommon
import crackers.kobots.buttonboard.Mode
import crackers.kobots.buttonboard.TheActions.HassActions
import crackers.kobots.buttonboard.TheActions.MopdiyActions
import crackers.kobots.parts.GOLDENROD
import crackers.kobots.parts.PURPLE
import crackers.kobots.parts.app.io.NeoKeyMenu
import crackers.kobots.parts.app.io.NeoKeyMenu.MenuItem
import crackers.kobots.parts.loadImage
import java.awt.Color
import java.awt.image.BufferedImage

/**
 * Handles what menu items are shown for the back "bench" (NeoKey) buttons.
 */
object BackBenchPicker : BenchPicker<Mode>(1, 1) {
    enum class HAImages(val image: BufferedImage) {
        BED(loadImage("/bed.png")),
        EXIT(loadImage("/exit.png")),
        LIGHTBULB(loadImage("/lightbulb.png")),
        MOON(loadImage("/moon.png")),
        MOVIE(loadImage("/movie.png")),
        RESTAURANT(loadImage("/restaurant.png")),
        SUN(loadImage("/sun.png")),
        TV(loadImage("/tv.png")),
        FAN(loadImage("/fan.png"));
    }

    val topMenuItem = MenuItem("Top", icon = HAImages.LIGHTBULB.image, buttonColor = Color.GREEN) { HassActions.TOP() }

    val morningMenuItem = MenuItem("Morn", icon = HAImages.SUN.image, buttonColor = GOLDENROD) {
        HassActions.MORNING()
        MopdiyActions.PLAY()
    }

    val fanControl = MenuItem("Fan", icon = HAImages.FAN.image, buttonColor = Color.BLUE) {
        HassActions.OFFICE_FAN()
        with(AppCommon.hasskClient) {
            (if (switch("small_fan").state().state == "off") MopdiyActions.PLAY else MopdiyActions.STOP)()
        }
    }

    val notAllOff = MenuItem("Off", icon = HAImages.EXIT.image, buttonColor = Color.DARK_GRAY) {
        HassActions.NOT_ALL()
        MopdiyActions.STOP()
    }

    val bedroom = MenuItem("Bed", icon = HAImages.BED.image, buttonColor = Color.PINK) { HassActions.BEDROOM() }

    override val menuSelections = mapOf(
        Mode.NIGHT to NeoKeyMenu(
            keyHandler,
            display,
            listOf(
                notAllOff,
                morningMenuItem,
                bedroom,
                topMenuItem,
            ),
        ),
        Mode.MORNING to NeoKeyMenu(
            keyHandler,
            display,
            listOf(
                topMenuItem,
                morningMenuItem,
                MenuItem("Kit", icon = HAImages.RESTAURANT.image, buttonColor = Color.CYAN) { HassActions.KITCHEN() },
                fanControl,
            ),
        ),
        Mode.DAYTIME to
            NeoKeyMenu(
                keyHandler,
                display,
                listOf(
                    topMenuItem,
                    MenuItem("Dim", buttonColor = GOLDENROD) { HassActions.TV() },
                    bedroom,
                    MenuItem("TV", icon = HAImages.TV.image, buttonColor = PURPLE) {
                        HassActions.TV()
                        MopdiyActions.STOP()
                    },
                    MenuItem("Movie", icon = HAImages.MOVIE.image, buttonColor = Color.RED.darker()) {
                        HassActions.MOVIE()
                        MopdiyActions.STOP()
                    },
                    fanControl,
                    MenuItem("Stop", icon = loadImage("/cancel.png"), buttonColor = Color.ORANGE) {
                        AppCommon.applicationRunning = false
                    },
                ),
            ),
        Mode.EVENING to NeoKeyMenu(
            keyHandler,
            display,
            listOf(
                MenuItem("Bed", icon = HAImages.BED.image, buttonColor = Color.PINK) { HassActions.BEDTIME() },
                MenuItem("Late", icon = HAImages.MOON.image, buttonColor = Color.RED) { HassActions.LATE_NIGHT() },
                notAllOff,
                fanControl,
            ),
        ),
    )
}
