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

import com.diozero.api.I2CDeviceInterface
import com.diozero.devices.oled.SSD1306
import com.diozero.devices.oled.SsdOledCommunicationChannel
import crackers.kobots.parts.app.io.SmallMenuDisplay
import java.awt.image.BufferedImage

/**
 * Show menus
 */
class TheScreen(i2cDevice: I2CDeviceInterface) : SmallMenuDisplay(DisplayMode.ICONS) {
    val screen = let {
        val channel = SsdOledCommunicationChannel.I2cCommunicationChannel(i2cDevice)
        SSD1306(channel, SSD1306.Height.SHORT).apply {
            clear()
            setDisplayOn(true)
            setContrast(0x20.toByte())
        }
    }

    override fun displayFun(menuImage: BufferedImage) {
        screen.setContrast((if (currentMode == Mode.NIGHT) 0x01 else 0x20).toByte())
        screen.display(menuImage)
    }

    fun close() {
        screen.clear()
        screen.setDisplayOn(false)
        screen.close()
    }
}
