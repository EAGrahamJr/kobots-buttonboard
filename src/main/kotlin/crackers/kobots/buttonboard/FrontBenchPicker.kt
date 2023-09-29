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

import com.diozero.devices.oled.SSD1306
import crackers.kobots.app.AppCommon
import crackers.kobots.devices.io.NeoKey
import crackers.kobots.parts.ORANGISH
import crackers.kobots.parts.PURPLE
import crackers.kobots.parts.app.io.NeoKeyHandler
import crackers.kobots.parts.app.io.NeoKeyMenu
import crackers.kobots.parts.loadImage
import org.slf4j.LoggerFactory
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles what menu items are shown for the front "bench" (NeoKey) buttons.
 */
object FrontBenchPicker {
    enum class FrontBenchActions {
        NONE, STANDARD_ROBOT, SHOW_OFF
    }

    internal val handlerDos: NeoKeyHandler by lazy {
        val kb2Device = i2cMultiplexer.getI2CDevice(3, NeoKey.DEFAULT_I2C_ADDRESS)
        val keyboardDos = NeoKey(kb2Device).apply { brightness = 0.01f }
        NeoKeyHandler(keyboardDos)
    }
    internal val displayDos: TheScreen by lazy {
        TheScreen(i2cMultiplexer.getI2CDevice(4, SSD1306.DEFAULT_I2C_ADDRESS))
    }

    private val logger = LoggerFactory.getLogger("FrontBenchPicker")
    private val menuSelections = ConcurrentHashMap(
        mapOf(
            FrontBenchActions.STANDARD_ROBOT to NeoKeyMenu(
                handlerDos,
                displayDos,
                listOf(
                    NeoKeyMenu.MenuItem(
                        "Drops",
                        icon = loadImage("/robot/symptoms.png"),
                        buttonColor = Color.DARK_GRAY,
                    ) { TheActions.GripperActions.PICKUP.execute() },
                    NeoKeyMenu.MenuItem(
                        "Rtn",
                        icon = loadImage("/robot/redo.png"),
                        buttonColor = Color.GREEN,
                    ) { TheActions.GripperActions.RETURN.execute() },
                    NeoKeyMenu.MenuItem(
                        "Hi",
                        icon = loadImage("/robot/hail.png"),
                        buttonColor = Color.CYAN.darker(),
                    ) { TheActions.GripperActions.SAY_HI.execute() },
                    NeoKeyMenu.MenuItem(
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
                handlerDos,
                displayDos,
                listOf(
                    NeoKeyMenu.MenuItem(
                        "Home",
                        buttonColor = Color.GREEN,
                        icon = loadImage("/robot/home.png"),
                    ) { TheActions.GripperActions.HOME.execute() },
                    NeoKeyMenu.MenuItem(
                        "Excuse Me",
                        abbrev = "Sorry",
                        icon = loadImage("/robot/cancel.png"),
                        buttonColor = PURPLE,
                    ) { TheActions.GripperActions.EXCUSE_ME.execute() },
                    NeoKeyMenu.MenuItem(
                        "Sleep",
                        icon = loadImage("/bed.png"),
                        buttonColor = ORANGISH,
                    ) { TheActions.GripperActions.SLEEP.execute() },
                    NeoKeyMenu.MenuItem(
                        "Stop",
                        icon = loadImage("/robot/halt.png"),
                        buttonColor = Color.YELLOW,
                        action = {
                            TheActions.GripperActions.STOP.execute()
                            updateMenu()
                        },
                    ),
                ),
            ),
        ),
    )

    fun currentMenu() = menuSelections[currentMenu] ?: menuSelections[FrontBenchActions.STANDARD_ROBOT]!!
    fun start() = currentMenu().displayMenu()
    fun stop() {
        displayDos.screen.setDisplayOn(false)
        handlerDos.buttonColors = listOf(Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK)
    }

    private var currentMenu = FrontBenchActions.STANDARD_ROBOT

    fun updateMenu() {
        currentMenu = when (currentMenu) {
            FrontBenchActions.STANDARD_ROBOT -> FrontBenchActions.SHOW_OFF
            FrontBenchActions.SHOW_OFF -> FrontBenchActions.STANDARD_ROBOT
            else -> FrontBenchActions.STANDARD_ROBOT
        }
        currentMenu().displayMenu()
    }
}
