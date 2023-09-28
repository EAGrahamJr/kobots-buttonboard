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
import crackers.kobots.buttonboard.TheActions.mqttClient
import crackers.kobots.devices.expander.I2CMultiplexer
import crackers.kobots.devices.io.NeoKey
import crackers.kobots.parts.app.KobotSleep
import crackers.kobots.parts.app.io.NeoKeyHandler
import crackers.kobots.parts.app.io.NeoKeyMenu
import org.slf4j.LoggerFactory
import java.awt.Color
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.exitProcess

const val REMOTE_PI = "diozero.remote.hostname"

private val logger = LoggerFactory.getLogger("ButtonBox")

internal var runFlag: Boolean
    get() = AppCommon.runFlag.get()
    private set(b) = AppCommon.runFlag.set(b)

private val _currentMode = AtomicReference(Mode.NIGHT)
internal var currentMode: Mode
    get() = _currentMode.get()
    private set(m) = _currentMode.set(m)

private var _remote: Boolean = false
internal val isRemote: Boolean
    get() = _remote

internal val multiplexor by lazy { I2CMultiplexer() }

internal lateinit var handlerUno: NeoKeyHandler
internal lateinit var displayUno: TheScreen
internal lateinit var handlerDos: NeoKeyHandler
internal lateinit var displayDos: TheScreen

/**
 * Uses NeoKey 1x4 as a HomeAssistant controller (and likely other things).
 */
fun main(args: Array<String>) {
    _remote = if (args.isNotEmpty()) {
        System.setProperty(REMOTE_PI, args[0])
        true
    } else {
        false
    }

    val kb1Device = multiplexor.getI2CDevice(0, NeoKey.DEFAULT_I2C_ADDRESS)
    val keyboardUno = NeoKey(kb1Device).apply { brightness = 0.01f }
    handlerUno = NeoKeyHandler(keyboardUno)
    displayUno = TheScreen(multiplexor.getI2CDevice(7, SSD1306.DEFAULT_I2C_ADDRESS))

    val kb2Device = multiplexor.getI2CDevice(3, NeoKey.DEFAULT_I2C_ADDRESS)
    val keyboardDos = NeoKey(kb2Device).apply { brightness = 0.01f }
    handlerDos = NeoKeyHandler(keyboardDos)
    displayDos = TheScreen(multiplexor.getI2CDevice(4, SSD1306.DEFAULT_I2C_ADDRESS))

    TheStrip.start()
    EnvironmentDisplay.start()
    RobotMenu.displayMenu()

    mqttClient.startAliveCheck()
    var currentMenu: NeoKeyMenu? = null

    while (runFlag) {
        try {
            // adjust per time of day
            val hour = LocalTime.now().hour
            val mode = when {
                hour in (0..6) -> Mode.NIGHT
                hour <= 8 -> Mode.MORNING
                hour <= 20 -> Mode.DAYTIME
                else -> Mode.EVENING
            }
            // mode change or error cleared
            if (mode != currentMode) {
                keyboardUno.brightness = mode.brightness
                keyboardDos.brightness = mode.brightness
                RobotMenu.displayMenu()
                currentMenu = ModeMenus[mode]!!
                currentMenu.displayMenu()
                currentMode = mode
            }

            if (!currentMenu!!.firstButton()) RobotMenu.firstButton()
        } catch (e: Throwable) {
            logger.error("Error found - continuing", e)
        }
        KobotSleep.millis(100)
    }
    keyboardUno.fill(Color.RED)
    displayUno.close()
    keyboardDos.fill(Color.BLACK)
    displayDos.close()
    logger.warn("Exiting ")

//    client.close()
    EnvironmentDisplay.stop()
    if (!isRemote) TheStrip.stop()
    keyboardUno.fill(Color.BLACK)
    multiplexor.close()
    AppCommon.executor.shutdownNow()
    exitProcess(0)
}
