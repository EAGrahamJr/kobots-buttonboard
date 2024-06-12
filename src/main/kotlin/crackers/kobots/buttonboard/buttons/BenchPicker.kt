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
import crackers.kobots.app.AppCommon
import crackers.kobots.buttonboard.GraphicsStuff
import crackers.kobots.buttonboard.i2cMultiplexer
import crackers.kobots.devices.io.NeoKey
import crackers.kobots.devices.lighting.PixelBuf
import crackers.kobots.parts.app.io.NeoKeyHandler
import crackers.kobots.parts.app.io.NeoKeyMenu
import crackers.kobots.parts.scheduleWithFixedDelay
import org.slf4j.LoggerFactory
import java.awt.Color
import java.util.concurrent.Future
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Synchronize screen, menu, and keyboard.
 */
abstract class BenchPicker<M : Enum<M>>(handlerChannel: Int, screenChannel: Int) {
    val keyHandler: NeoKeyHandler
    val keyBoard: NeoKey
    val display: TheScreen
    protected abstract val menuSelections: Map<M, NeoKeyMenu>
    protected lateinit var nowMenu: M
    protected lateinit var menuEnumConstants: Array<M>

    private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    init {
        with(i2cMultiplexer) {
            val kbDevice = getI2CDevice(handlerChannel, NeoKey.DEFAULT_I2C_ADDRESS)
            keyBoard = NeoKey(kbDevice).apply { brightness = 0.01f }
            keyHandler = NeoKeyHandler(keyBoard, initialBrightness = 0.01f)
            display = TheScreen(getI2CDevice(screenChannel, SSD1306.DEFAULT_I2C_ADDRESS))
        }
    }

    val stahp =
        NeoKeyMenu.MenuItem("Stop", icon = GraphicsStuff.CANCEL_ICON, buttonColor = Color.RED) {
            AppCommon.applicationRunning = false
        }

    val currentMenu: NeoKeyMenu
        @Synchronized
        get() {
            if (!::nowMenu.isInitialized) {
                nowMenu = menuSelections.keys.first()
                menuEnumConstants = nowMenu.declaringJavaClass.enumConstants
            }
            return menuSelections[nowMenu] ?: run {
                logger.error("No menu for $nowMenu")
                nowMenu = menuSelections.keys.first()
                logger.info("Switching to $nowMenu")
                menuSelections[nowMenu]!!
            }
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
        val next = (nowMenu.ordinal + 1) % menuEnumConstants.size
        nowMenu = menuEnumConstants[next]
        currentMenu.displayMenu()
        logger.info("Switching to $nowMenu")
    }

    @Synchronized
    fun selectMenu(whichOne: M) {
        nowMenu = whichOne
        currentMenu.displayMenu()
    }

    companion object {
    }

    class Blinker(private val pixelBuf: PixelBuf, private val blinkOffColor: Color = Color.RED) {
        private val notRunning = -1

        // TODO allow more than one button to blink
        private var blinkyFuture: Future<*>? = null
        private var blinkyState = false
        private lateinit var ogColor: Color

        @Volatile
        private var blinkingKeyIndex: Int = notRunning

        fun start(
            index: Int,
            color: Color? = null,
            blinkTime: Duration = 500.milliseconds,
        ) {
            require(blinkingKeyIndex == notRunning) { "Blinker already started" }
            ogColor = color ?: pixelBuf[index].color
            blinkingKeyIndex = index
            blinkyFuture =
                AppCommon.executor.scheduleWithFixedDelay(blinkTime, blinkTime) {
                    blinkyState =
                        !blinkyState.also {
                            pixelBuf[index] = if (it) blinkOffColor else ogColor
                        }
                }
        }

        fun stop() {
            if (blinkingKeyIndex > notRunning) {
                pixelBuf[blinkingKeyIndex] = ogColor
                blinkingKeyIndex = notRunning
                blinkyFuture?.cancel(true)
                blinkyFuture = null
            }
        }
    }
}
