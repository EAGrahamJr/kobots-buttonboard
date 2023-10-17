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
import crackers.kobots.buttonboard.audioPause
import crackers.kobots.buttonboard.audioPlay
import crackers.kobots.buttonboard.volumeDown
import crackers.kobots.buttonboard.volumeUp
import crackers.kobots.parts.ORANGISH
import crackers.kobots.parts.app.io.NeoKeyMenu
import crackers.kobots.parts.app.io.NeoKeyMenu.MenuItem
import crackers.kobots.parts.loadImage
import java.awt.Color

enum class FrontBenchActions {
    NONE, STANDARD_ROBOT, SHOW_OFF, MOPIDI
}

/**
 * Handles what menu items are shown for the front "bench" (NeoKey) buttons.
 */
object FrontBenchPicker : BenchPicker<FrontBenchActions>(0, 0) {
    val REDO_IMAGE = loadImage("/robot/redo.png")

    val STOP_IT = loadImage("/robot/halt.png")

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
                MenuItem("Drops", icon = loadImage("/robot/symptoms.png"), buttonColor = Color.DARK_GRAY) {
                    GripperActions.PICKUP()
                },
                MenuItem("Rtn", icon = REDO_IMAGE, buttonColor = Color.GREEN) {
                    GripperActions.RETURN()
                },
                MenuItem("Hi", icon = loadImage("/robot/hail.png"), buttonColor = DARK_CYAH) {
                    GripperActions.SAY_HI()
                },
                MenuItem("Exit", icon = loadImage("/robot/dangerous.png"), buttonColor = Color.RED) {
                    GripperActions.STOP()
                    AppCommon.applicationRunning = false
                },
            ),
        ),
        FrontBenchActions.SHOW_OFF to NeoKeyMenu(
            keyHandler,
            display,
            listOf(
                MenuItem("Home", buttonColor = Color.GREEN, icon = loadImage("/robot/home.png")) {
                    GripperActions.HOME()
                },
                MenuItem(
                    "Excuse Me",
                    abbrev = "Sorry",
                    icon = loadImage("/robot/cancel.png"),
                    buttonColor = DARK_CYAH
                ) { GripperActions.EXCUSE_ME() },
                MenuItem("Sleep", icon = BackBenchPicker.HAImages.BED.image, buttonColor = Color.BLUE.darker()) {
                    GripperActions.SLEEP()
                },
                MenuItem("Stop", icon = STOP_IT, buttonColor = ORANGISH) {
                    GripperActions.STOP()
                },
                MenuItem("Flash", icon = loadImage("/robot/flashlight_on.png"), buttonColor = Color.YELLOW) {
                    GripperActions.FLASHLIGHT()
                },
            ),
        ),
        FrontBenchActions.MOPIDI to audioPlayMenu,
    )
}
