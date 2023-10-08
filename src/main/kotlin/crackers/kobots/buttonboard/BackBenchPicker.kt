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

import crackers.kobots.app.AppCommon
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
object BackBenchPicker : BenchPicker<Mode>(0, 7) {
    private enum class Images(val image: BufferedImage) {
        BED(loadImage("/bed.png")),
        EXIT(loadImage("/exit.png")),
        LIGHTBULB(loadImage("/lightbulb.png")),
        MOON(loadImage("/moon.png")),
        MOVIE(loadImage("/movie.png")),
        RESTAURANT(loadImage("/restaurant.png")),
        SUN(loadImage("/sun.png")),
        TV(loadImage("/tv.png")),
        FAN(loadImage("/fan.png")),
    }

    val theFanThing = {
        HassActions.OFFICE_FAN.execute()
        with(AppCommon.hasskClient) {
            (if (switch("small_fan").state().state == "off") MopdiyActions.PLAY else MopdiyActions.STOP).execute()
        }
    }
    override val menuSelections = mapOf(
        Mode.NIGHT to NeoKeyMenu(
            keyHandler,
            display,
            listOf(
                MenuItem(
                    "Off",
                    icon = Images.EXIT.image,
                    buttonColor = Color.DARK_GRAY,
                    action = {
                        HassActions.NOT_ALL.execute()
                        MopdiyActions.STOP.execute()
                    },
                ),
                MenuItem(
                    "Top",
                    icon = Images.LIGHTBULB.image,
                    buttonColor = Color.GREEN,
                    action = { HassActions.TOP.execute() },
                ),
                MenuItem(
                    "Morn",
                    icon = Images.SUN.image,
                    buttonColor = GOLDENROD,
                    action = {
                        HassActions.MORNING.execute()
                        MopdiyActions.PLAY.execute()
                    },
                ),
                MenuItem(
                    "Bed",
                    icon = Images.BED.image,
                    buttonColor = Color.PINK,
                    action = { HassActions.BEDROOM.execute() },
                ),
            ),
        ),
        Mode.MORNING to NeoKeyMenu(
            keyHandler,
            display,
            listOf(
                MenuItem(
                    "Morn",
                    icon = Images.SUN.image,
                    buttonColor = GOLDENROD,
                    action = {
                        HassActions.MORNING.execute()
                        MopdiyActions.PLAY.execute()
                    },
                ),
                MenuItem(
                    "Top",
                    icon = Images.LIGHTBULB.image,
                    buttonColor = Color.GREEN,
                    action = {
                        HassActions.TOP.execute()
                        MopdiyActions.PLAY.execute()
                    },
                ),
                MenuItem(
                    "Kit",
                    icon = Images.RESTAURANT.image,
                    buttonColor = Color.CYAN,
                    action = { HassActions.KITCHEN.execute() },
                ),
                MenuItem(
                    "Fan",
                    icon = Images.FAN.image,
                    buttonColor = Color.BLUE,
                    action = theFanThing,
                ),
            ),
        ),
        Mode.DAYTIME to
            NeoKeyMenu(
                keyHandler,
                display,
                listOf(
                    MenuItem(
                        "Top",
                        icon = Images.LIGHTBULB.image,
                        buttonColor = Color.GREEN,
                        action = {
                            HassActions.TOP.execute()
                            MopdiyActions.PLAY.execute()
                        },
                    ),
                    MenuItem(
                        "TV",
                        icon = Images.TV.image,
                        buttonColor = PURPLE,
                        action = {
                            HassActions.TV.execute()
                            MopdiyActions.STOP.execute()
                        },
                    ),
                    MenuItem(
                        "Movie",
                        icon = Images.MOVIE.image,
                        buttonColor = Color.RED,
                        action = {
                            HassActions.MOVIE.execute()
                            MopdiyActions.STOP.execute()
                        },
                    ),
                    MenuItem(
                        "Fan",
                        icon = Images.FAN.image,
                        buttonColor = Color.BLUE,
                        action = theFanThing,
                    ),
                ),
            ),
        Mode.EVENING to NeoKeyMenu(
            keyHandler,
            display,
            listOf(
                MenuItem(
                    "Bed",
                    icon = Images.BED.image,
                    buttonColor = Color.PINK,
                    action = { HassActions.BEDTIME.execute() },
                ),
                MenuItem(
                    "Late",
                    icon = Images.MOON.image,
                    buttonColor = Color.RED,
                    action = { HassActions.LATE_NIGHT.execute() },
                ),
                MenuItem(
                    "Off",
                    icon = Images.EXIT.image,
                    buttonColor = Color.DARK_GRAY,
                    action = {
                        HassActions.NOT_ALL.execute()
                        MopdiyActions.STOP.execute()
                    },
                ),
                MenuItem(
                    "Fan",
                    icon = Images.FAN.image,
                    buttonColor = Color.BLUE,
                    action = theFanThing,
                ),
            ),
        ),
    )
}
