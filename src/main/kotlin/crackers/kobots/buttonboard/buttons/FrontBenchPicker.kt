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
import crackers.kobots.buttonboard.TheActions.GripperActions
import crackers.kobots.buttonboard.buttons.BenchPicker.Companion.HAImages
import crackers.kobots.buttonboard.buttons.BenchPicker.Companion.RobotImages
import crackers.kobots.parts.ORANGISH
import crackers.kobots.parts.app.io.NeoKeyMenu
import crackers.kobots.parts.app.io.NeoKeyMenu.MenuItem
import java.awt.Color

enum class FrontBenchActions {
    NONE, STANDARD_ROBOT, SHOW_OFF, MOPIDI
}

/**
 * Handles what menu items are shown for the front "bench" (NeoKey) buttons.
 */
object FrontBenchPicker : BenchPicker<FrontBenchActions>(0, 0) {
    /**
     * Set as a discrete object so we can tell when it's activated.
     */
    val audioPlayMenu = NeoKeyMenu(
        keyHandler,
        display,
        listOf(
            audioPlay,
            audioPause,
            volumeUp,
            volumeDown,
//            NO_KEY,
//            MenuItem("Stop", icon = STOP_IT, buttonColor = ORANGISH) {
//                GestureSensor.volumeMode = false
//                updateMenu()
//            },
        ),
    )
    val DARK_CYAH = Color.CYAN.darker()
    override val menuSelections = mapOf(
        FrontBenchActions.STANDARD_ROBOT to NeoKeyMenu(
            keyHandler,
            display,
            listOf(
                MenuItem("Drops", icon = RobotImages.DROPS.image, buttonColor = Color.DARK_GRAY) {
                    GripperActions.PICKUP()
                },
                MenuItem("Rtn", icon = RobotImages.RETURN.image, buttonColor = Color.GREEN) {
                    GripperActions.RETURN()
                },
                MenuItem("Hi", icon = RobotImages.HI.image, buttonColor = DARK_CYAH) {
                    GripperActions.SAY_HI()
                },
                MenuItem("Exit", icon = RobotImages.STOP.image, buttonColor = Color.RED) {
                    GripperActions.STOP()
                    AppCommon.applicationRunning = false
                },
            ),
        ),
        FrontBenchActions.SHOW_OFF to NeoKeyMenu(
            keyHandler,
            display,
            listOf(
                MenuItem("Home", buttonColor = Color.GREEN, icon = RobotImages.HOME.image) {
                    GripperActions.HOME()
                },
                MenuItem("Excuse Me", "Sry", CANCEL_ICON, DARK_CYAH) { GripperActions.EXCUSE_ME() },
                MenuItem("Sleep", icon = HAImages.BED.image, buttonColor = Color.BLUE.darker()) {
                    GripperActions.SLEEP()
                },
                MenuItem("Stop", icon = RobotImages.STOP_IT.image, buttonColor = ORANGISH) {
                    GripperActions.STOP()
                },
                MenuItem("Flash", icon = RobotImages.FLASHLIGHT.image, buttonColor = Color.YELLOW) {
                    GripperActions.FLASHLIGHT()
                },
            ),
        ),
        FrontBenchActions.MOPIDI to audioPlayMenu,
    )
}
