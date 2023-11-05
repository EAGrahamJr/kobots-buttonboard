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

import crackers.kobots.buttonboard.Mode
import crackers.kobots.buttonboard.currentMode
import crackers.kobots.buttonboard.i2cMultiplexer
import crackers.kobots.buttonboard.killAllTheThings
import crackers.kobots.devices.io.QwiicTwist
import crackers.kobots.devices.io.QwiicTwist.Companion.PRESSED
import crackers.kobots.parts.GOLDENROD
import crackers.kobots.parts.PURPLE
import java.awt.Color

/**
 * Rotary encoder stuff.
 */
object RotoRegulator {

    val encoder by lazy { QwiicTwist(i2cMultiplexer.getI2CDevice(2, QwiicTwist.DEFAULT_I2C_ADDRESS)) }

    init {
        encoder.count = 0 // always start at zero
        encoder.pixel.brightness = 0.25f
        encoder.pixel + PURPLE
        encoder.colorConnection = Triple(0, 0, 0)
    }

    private var lastCount = IntRange.EMPTY
    private var buttonDown = false

    fun setPixelColor() {
        val color = when (lastCount) {
            IntRange.EMPTY -> Color.BLACK
            midRange -> GOLDENROD
            lowRange -> PURPLE
            highRange -> Color.GREEN
            else -> Color.RED
        }

        encoder.pixel + color
        encoder.pixel.brightness = when (currentMode) {
            Mode.NIGHT -> 0.01f
            Mode.MORNING -> 0.05f
            Mode.DAYTIME -> 0.25f
            Mode.EVENING -> 0.05f
            else -> 0.01f
        }
        // TODO only do this when it's "adjusting" something
//        encoder.colorConnection = Triple(25, 25, 25)
    }

    val midRange = -2..2
    val lowRange = -100..-3
    val highRange = 3..100

    fun readTwist() {
        buttonDown = (encoder button PRESSED).also { pressed -> if (pressed && !buttonDown) killAllTheThings() }

        val count = encoder.count
        if (count in lastCount) return

        lastCount = when {
            count in midRange -> {
                FrontBenchPicker.selectMenu(FrontBenchActions.STANDARD_ROBOT)
                midRange
            }

            count in lowRange -> {
                FrontBenchPicker.selectMenu(FrontBenchActions.SHOW_OFF)
                lowRange
            }

            count in highRange -> {
                FrontBenchPicker.selectMenu(FrontBenchActions.MOPIDI)
                highRange
            }

            else -> {
                FrontBenchPicker.selectMenu(FrontBenchActions.STANDARD_ROBOT)
                IntRange.EMPTY
            }
        }
        setPixelColor()
    }
}
