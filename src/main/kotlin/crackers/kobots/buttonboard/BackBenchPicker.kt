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

import crackers.kobots.parts.GOLDENROD
import crackers.kobots.parts.PURPLE
import crackers.kobots.parts.app.io.NeoKeyMenu
import crackers.kobots.parts.loadImage
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.concurrent.ConcurrentHashMap

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

    override val menuSelections = ConcurrentHashMap(
        mapOf(
            Mode.NIGHT to NeoKeyMenu(
                keyHandler,
                display,
                listOf(
                    NeoKeyMenu.MenuItem(
                        "Off",
                        icon = Images.EXIT.image,
                        buttonColor = Color.DARK_GRAY,
                        action = { TheActions.HassActions.NOT_ALL.execute() },
                    ),
                    NeoKeyMenu.MenuItem(
                        "Top",
                        icon = Images.LIGHTBULB.image,
                        buttonColor = Color.GREEN,
                        action = { TheActions.HassActions.TOP.execute() },
                    ),
                    NeoKeyMenu.MenuItem(
                        "Morn",
                        icon = Images.SUN.image,
                        buttonColor = GOLDENROD,
                        action = { TheActions.HassActions.MORNING.execute() },
                    ),
                    NeoKeyMenu.MenuItem(
                        "Bed",
                        icon = Images.BED.image,
                        buttonColor = Color.PINK,
                        action = { TheActions.HassActions.BEDROOM.execute() },
                    ),
                ),
            ),
            Mode.MORNING to NeoKeyMenu(
                keyHandler,
                display,
                listOf(
                    NeoKeyMenu.MenuItem(
                        "Morn",
                        icon = Images.SUN.image,
                        buttonColor = GOLDENROD,
                        action = { TheActions.HassActions.MORNING.execute() },
                    ),
                    NeoKeyMenu.MenuItem(
                        "Top",
                        icon = Images.LIGHTBULB.image,
                        buttonColor = Color.GREEN,
                        action = { TheActions.HassActions.TOP.execute() },
                    ),
                    NeoKeyMenu.MenuItem(
                        "Kit",
                        icon = Images.RESTAURANT.image,
                        buttonColor = Color.CYAN,
                        action = { TheActions.HassActions.KITCHEN.execute() },
                    ),
                    NeoKeyMenu.MenuItem(
                        "Fan",
                        icon = Images.FAN.image,
                        buttonColor = Color.BLUE,
                        action = { TheActions.HassActions.OFFICE_FAN.execute() },
                    ),
                ),

                ),
            Mode.DAYTIME to
                NeoKeyMenu(
                    keyHandler,
                    display,
                    listOf(
                        NeoKeyMenu.MenuItem(
                            "Top",
                            icon = Images.LIGHTBULB.image,
                            buttonColor = Color.GREEN,
                            action = { TheActions.HassActions.TOP.execute() },
                        ),
                        NeoKeyMenu.MenuItem(
                            "TV",
                            icon = Images.TV.image,
                            buttonColor = PURPLE,
                            action = { TheActions.HassActions.TV.execute() },
                        ),
                        NeoKeyMenu.MenuItem(
                            "Movie",
                            icon = Images.MOVIE.image,
                            buttonColor = Color.RED,
                            action = { TheActions.HassActions.MOVIE.execute() },
                        ),
                        NeoKeyMenu.MenuItem(
                            "Fan",
                            icon = Images.FAN.image,
                            buttonColor = Color.BLUE,
                            action = { TheActions.HassActions.OFFICE_FAN.execute() },
                        ),
                    ),

                    ),
            Mode.EVENING to NeoKeyMenu(
                keyHandler,
                display,
                listOf(
                    NeoKeyMenu.MenuItem(
                        "Bed",
                        icon = Images.BED.image,
                        buttonColor = Color.PINK,
                        action = { TheActions.HassActions.BEDTIME.execute() },
                    ),
                    NeoKeyMenu.MenuItem(
                        "Late",
                        icon = Images.MOON.image,
                        buttonColor = Color.RED,
                        action = { TheActions.HassActions.LATE_NIGHT.execute() },
                    ),
                    NeoKeyMenu.MenuItem(
                        "Off",
                        icon = Images.EXIT.image,
                        buttonColor = Color.DARK_GRAY,
                        action = { TheActions.HassActions.NOT_ALL.execute() },
                    ),
                    NeoKeyMenu.MenuItem(
                        "Fan",
                        icon = Images.FAN.image,
                        buttonColor = Color.BLUE,
                        action = { TheActions.HassActions.OFFICE_FAN.execute() },
                    ),
                ),
            ),
        ),

        )
}
