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
import crackers.kobots.buttonboard.i2cMultiplexer
import crackers.kobots.devices.io.NeoKey
import crackers.kobots.devices.lighting.PixelBuf
import crackers.kobots.parts.app.io.NeoKeyHandler
import crackers.kobots.parts.app.io.NeoKeyMenu
import crackers.kobots.parts.loadImage
import crackers.kobots.parts.scheduleWithFixedDelay
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.concurrent.Future
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

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
            return menuSelections[_currentMenu] ?: run {
                logger.error("No menu for $_currentMenu")
                _currentMenu = menuSelections.keys.first()
                menuSelections[_currentMenu]!!
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
        val next = (_currentMenu.ordinal + 1) % menuSelections.size
        _currentMenu = _currentMenu.declaringJavaClass.enumConstants[next]
        currentMenu.displayMenu()
    }

    @Synchronized
    fun selectMenu(whichOne: M) {
        _currentMenu = whichOne
        currentMenu.displayMenu()
    }

    companion object {
        val CANCEL_ICON = loadImage("/cancel.png")
        val DARK_CYAN = Color.CYAN.darker()

        enum class HAImages(val image: BufferedImage) {
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

        enum class RobotImages(val image: BufferedImage) {
            STOP(loadImage("/robot/dangerous.png")),
            FLASHLIGHT(loadImage("/robot/flashlight_on.png")),
            HI(loadImage("/robot/hail.png")),
            STOP_IT(loadImage("/robot/halt.png")),
            HOME(loadImage("/robot/home.png")),
            RETURN(loadImage("/robot/redo.png")),
            DROPS(loadImage("/robot/symptoms.png")),
            CLEAR(loadImage("/robot/restart_alt.png")),
        }
    }

    class Blinker(private val pixelBuf: PixelBuf, private val blinkOffColor: Color = Color.RED) {
        private val RUNNING = -1

        // TODO allow more than one button to blink
        private var blinkyFuture: Future<*>? = null
        private var blinkyState = false
        private lateinit var ogColor: Color

        @Volatile
        private var blinkingKeyIndex: Int = RUNNING

        fun start(
            index: Int,
            color: Color? = null,
            blinkTime: Duration = 500.milliseconds,
        ) {
            require(blinkingKeyIndex == RUNNING) { "Blinker already started" }
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
            if (blinkingKeyIndex > RUNNING) {
                pixelBuf[blinkingKeyIndex] = ogColor
                blinkingKeyIndex = RUNNING
                blinkyFuture?.cancel(true)
                blinkyFuture = null
            }
        }
    }
}
