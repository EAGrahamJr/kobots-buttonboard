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
import crackers.kobots.buttonboard.RosetteStatus
import crackers.kobots.buttonboard.TheActions.GripperActions
import crackers.kobots.buttonboard.buttons.BenchPicker.Companion.HAImages
import crackers.kobots.buttonboard.buttons.BenchPicker.Companion.RobotImages
import crackers.kobots.parts.ORANGISH
import crackers.kobots.parts.app.io.NeoKeyMenu
import crackers.kobots.parts.app.io.NeoKeyMenu.MenuItem
import crackers.kobots.parts.scheduleWithFixedDelay
import java.awt.Color
import java.util.concurrent.Future
import kotlin.time.Duration.Companion.milliseconds

enum class FrontBenchActions {
    STANDARD_ROBOT, SHOW_OFF, MOPIDI
}

/**
 * Handles what menu items are shown for the front "bench" (NeoKey) buttons.
 */
object FrontBenchPicker : BenchPicker<FrontBenchActions>(0, 0) {
    private val audioPlayMenu = NeoKeyMenu(
        keyHandler,
        display,
        listOf(
            audioPlay,
            audioPause,
            volumeUp,
            volumeDown,
        ),
    )

    private val DARK_CYAN = Color.CYAN.darker()

    private val homeItem = MenuItem("Home", buttonColor = Color.GREEN, icon = RobotImages.HOME.image) {
        GripperActions.HOME()
    }

    private lateinit var blinkyFuture: Future<*>
    private var blinkyState = false

    fun startBlinky() {
        blinkyFuture = AppCommon.executor.scheduleWithFixedDelay(500.milliseconds, 500.milliseconds) {
            blinkyState = !blinkyState
            if (blinkyState) {
                keyBoard[1] = Color.RED
            } else {
                keyBoard[1] = Color.GREEN
            }
        }
    }

    override val menuSelections = mapOf(
        FrontBenchActions.STANDARD_ROBOT to NeoKeyMenu(
            keyHandler,
            display,
            listOf(
                MenuItem("Drops", icon = RobotImages.DROPS.image, buttonColor = Color.DARK_GRAY) {
                    GripperActions.PICKUP()
                },
                MenuItem("Rtn", icon = RobotImages.RETURN.image, buttonColor = DARK_CYAN) {
                    GripperActions.RETURN()
                    if (::blinkyFuture.isInitialized) blinkyFuture.cancel(true)
                },
                homeItem,
                MenuItem(
                    "Clear",
                    icon = RobotImages.CLEAR.image,
                    buttonColor = Color.BLUE,
                    action = RosetteStatus::reset,
                ),
            ),
        ),
        FrontBenchActions.SHOW_OFF to NeoKeyMenu(
            keyHandler,
            display,
            listOf(
                homeItem,
                MenuItem("Excuse Me", "Sry", CANCEL_ICON, DARK_CYAN) { GripperActions.EXCUSE_ME() },
                MenuItem("Sleep", icon = HAImages.BED.image, buttonColor = Color.BLUE.darker()) {
                    GripperActions.SLEEP()
                },
                MenuItem("Hi", icon = RobotImages.HI.image, buttonColor = DARK_CYAN) {
                    GripperActions.SAY_HI()
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
