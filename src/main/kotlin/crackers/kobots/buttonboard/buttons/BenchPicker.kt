/*
 * Copyright 2022-2026 by E. A. Graham, Jr.
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
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_SKULL
import crackers.kobots.buttonboard.Mode
import crackers.kobots.buttonboard.RosetteStatus
import crackers.kobots.buttonboard.i2cMultiplexer
import crackers.kobots.devices.io.NeoKey
import crackers.kobots.parts.app.io.NeoKeyHandler
import crackers.kobots.parts.app.io.NeoKeyMenu
import crackers.kobots.parts.movement.async.EventBus
import crackers.kobots.parts.movement.async.NeoKeyEvent
import crackers.kobots.parts.movement.async.startAsync
import org.slf4j.LoggerFactory
import java.awt.Color
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.milliseconds

/**
 * Synchronize screen, menu, and keyboard.
 */
abstract class BenchPicker(
    handlerChannel: Int,
    screenChannel: Int,
) : AppCommon.Startable {
    val keyHandler: NeoKeyHandler
    val keyBoard: NeoKey
    val display: TheScreen
    protected abstract val menuSelections: Map<Mode, NeoKeyMenu>
    protected val nowMenu = AtomicReference<Mode>()
    protected val statusReset = NeoKeyMenu.MenuItem("Stat", icon = IMAGE_SKULL, action = { RosetteStatus.reset() })

    private val me = this.javaClass.simpleName
    private val logger = LoggerFactory.getLogger(me)

    init {
        with(i2cMultiplexer) {
            val kbDevice = getI2CDevice(handlerChannel, NeoKey.DEFAULT_I2C_ADDRESS)
            keyBoard = NeoKey(kbDevice).apply { brightness = 0.01f }
            keyHandler = NeoKeyHandler(keyBoard, initialBrightness = 0.01f, name = me)
            display = TheScreen(getI2CDevice(screenChannel, SSD1306.DEFAULT_I2C_ADDRESS))
        }
    }

    val stahp =
        NeoKeyMenu.MenuItem("Stop", icon = GraphicsStuff.CANCEL_ICON, buttonColor = Color.RED) {
            AppCommon.applicationRunning = false
        }

    override fun start() {
        // register an event listener on the EentBus for a NeioKey press
        EventBus.subscribe<NeoKeyEvent>(name = me, onEvent = { event ->
            menuSelections[nowMenu.get()]?.run {
                event.buttons.forEachIndexed { index, yes ->
                    if (yes) {
                        logger.info("Button $index pressed in menu ${nowMenu.get()}")
                        menuItems[index].action()
                    }
                }
            }
        })
        keyHandler.startAsync(50.milliseconds)
        selectMenu(menuSelections.keys.first())
    }

    override fun stop() {
        keyHandler.buttonColors = listOf(Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK)
        display.close()
    }

    @Synchronized
    fun selectMenu(whichOne: Mode) {
        val keys = menuSelections.keys
        val selected = if (keys.contains(whichOne)) whichOne else keys.first()
        nowMenu.set(selected)
        keyHandler.brightness = selected.brightness
        menuSelections[selected]!!.displayMenu()
    }

    protected fun makeAMenu(items: List<NeoKeyMenu.MenuItem>) = NeoKeyMenu(keyHandler, display, items)
}

// 3644120 139312  35680 S  23.8  15.0
