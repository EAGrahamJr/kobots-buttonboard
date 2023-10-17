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

import com.diozero.devices.oled.SSD1306
import crackers.kobots.buttonboard.i2cMultiplexer
import crackers.kobots.devices.io.NeoKey
import crackers.kobots.parts.app.io.NeoKeyHandler
import crackers.kobots.parts.app.io.NeoKeyMenu
import org.slf4j.LoggerFactory
import java.awt.Color

/**
 * Synchronize screen, menu, and keyboard.
 */
abstract class BenchPicker<M : Enum<M>>(handlerChannel: Int, screenChannel: Int) {
    val keyHandler: NeoKeyHandler
    protected val keyBoard: NeoKey
    protected val display: TheScreen
    protected abstract val menuSelections: Map<M, NeoKeyMenu>
    protected lateinit var _currentMenu: M

    private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    init {
        with(i2cMultiplexer) {
            val kbDevice = getI2CDevice(handlerChannel, NeoKey.DEFAULT_I2C_ADDRESS)
            keyBoard = NeoKey(kbDevice).apply { brightness = 0.01f }
            keyHandler = NeoKeyHandler(keyBoard)

            display = TheScreen(getI2CDevice(screenChannel, SSD1306.DEFAULT_I2C_ADDRESS))
        }
    }

    val currentMenu: NeoKeyMenu
        @Synchronized
        get() {
            if (!::_currentMenu.isInitialized) _currentMenu = menuSelections.keys.first()
            return menuSelections[_currentMenu]!!
        }

    fun start() = currentMenu.displayMenu()
    fun stop() {
        keyHandler.buttonColors = listOf(Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK)
        display.close()
    }

    /**
     * "Rotates" to the next menu item in ordinal order.
     */
    @Synchronized
    fun updateMenu() {
        val next = menuSelections.keys.indexOf(_currentMenu) + 1
        _currentMenu = menuSelections.keys.elementAt(next % menuSelections.size)
        currentMenu.displayMenu()
    }

    @Synchronized
    fun selectMenu(whichOne: M) {
        _currentMenu = whichOne
        currentMenu.displayMenu()
    }
}
