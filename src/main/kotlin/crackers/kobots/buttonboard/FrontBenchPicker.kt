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
import crackers.kobots.parts.ORANGISH
import crackers.kobots.parts.app.io.NeoKeyMenu
import crackers.kobots.parts.app.io.NeoKeyMenu.MenuItem
import crackers.kobots.parts.loadImage
import java.awt.Color

enum class FrontBenchActions {
    NONE, STANDARD_ROBOT, SHOW_OFF
}

/**
 * Handles what menu items are shown for the front "bench" (NeoKey) buttons.
 */
object FrontBenchPicker : BenchPicker<FrontBenchActions>(0, 0) {
    override val menuSelections = mapOf(
        FrontBenchActions.STANDARD_ROBOT to NeoKeyMenu(
            keyHandler,
            display,
            listOf(
                MenuItem(
                    "Drops",
                    icon = loadImage("/robot/symptoms.png"),
                    buttonColor = Color.DARK_GRAY,
                ) { TheActions.GripperActions.PICKUP.execute() },
                MenuItem(
                    "Rtn",
                    icon = loadImage("/robot/redo.png"),
                    buttonColor = Color.GREEN,
                ) { TheActions.GripperActions.RETURN.execute() },
                MenuItem(
                    "Hi",
                    icon = loadImage("/robot/hail.png"),
                    buttonColor = Color.CYAN.darker(),
                ) { TheActions.GripperActions.SAY_HI.execute() },
                MenuItem(
                    "Exit",
                    icon = loadImage("/robot/dangerous.png"),
                    buttonColor = Color.RED,
                ) {
                    TheActions.GripperActions.STOP.execute()
                    AppCommon.applicationRunning = false
                },
            ),
        ),
        FrontBenchActions.SHOW_OFF to NeoKeyMenu(
            keyHandler,
            display,
            listOf(
                MenuItem(
                    "Home",
                    buttonColor = Color.GREEN,
                    icon = loadImage("/robot/home.png"),
                ) { TheActions.GripperActions.HOME.execute() },
                MenuItem(
                    "Excuse Me",
                    abbrev = "Sorry",
                    icon = loadImage("/robot/cancel.png"),
                    buttonColor = Color.CYAN.darker(),
                ) { TheActions.GripperActions.EXCUSE_ME.execute() },
                MenuItem(
                    "Sleep",
                    icon = BackBenchPicker.HAImages.BED.image,
                    buttonColor = Color.BLUE.darker(),
                ) { TheActions.GripperActions.SLEEP.execute() },
                MenuItem(
                    "Stop",
                    icon = loadImage("/robot/halt.png"),
                    buttonColor = ORANGISH,
                ) {
                    TheActions.GripperActions.STOP.execute()
                    updateMenu()
                },
                MenuItem(
                    "Flash",
                    icon = loadImage("/robot/flashlight_on.png"),
                    buttonColor = Color.YELLOW,
                ) { TheActions.GripperActions.FLASHLIGHT.execute() },
            ),
        ),
    )
}
