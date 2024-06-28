/*
 * Copyright 2022-2024 by E. A. Graham, Jr.
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

import crackers.kobots.app.AppCommon
import crackers.kobots.devices.io.QwiicTwist
import crackers.kobots.parts.GOLDENROD
import java.awt.Color
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock

/**
 * Rotary encoder.
 */
object Rooty : AppCommon.Startable {
    private lateinit var encoder: QwiicTwist

    enum class TwistMode {
        NIGHT,
        PANIC,
        AUDIO,
        LIGHT,
        OFF,
    }

    override fun start() {
        encoder =
            QwiicTwist(i2cMultiplexer.getI2CDevice(4, QwiicTwist.DEFAULT_I2C_ADDRESS)).apply {
                pixel.brightness = .1f
                pixel.fill(Color.YELLOW)
                clearInterrupts() // clear buffers
            }
    }

    private var lastMode = Mode.NONE
    private val realTwist = AtomicReference(TwistMode.OFF)
    var twistMode: TwistMode
        get() = realTwist.get()
        set(v) {
            println("Update twist mode $v")
            realTwist.set(v)
        }
    private var lastTwist = TwistMode.OFF
    private var mediaSource = AtomicReference("edlap")
    var currentMediaSource: String
        get() = mediaSource.get()
        private set(v) {
            mediaSource.set(v)
        }

    private val syncLock = ReentrantLock()

    fun clickOrTwist(): Boolean {
        if (lastMode != currentMode) {
            // check if we need to change what we're doing
            lastMode = currentMode
            twistMode =
                when (currentMode) {
                    Mode.NONE -> TwistMode.OFF
                    Mode.NIGHT -> TwistMode.NIGHT
                    Mode.AUDIO -> TwistMode.AUDIO
                    else -> TwistMode.PANIC
                }
        }
        // effect any changes necessary
        if (twistMode != lastTwist) {
            lastTwist = twistMode
            with(encoder.pixel) {
                when (twistMode) {
                    TwistMode.NIGHT -> {
                        fill(Color.PINK)
                        brightness = .03f
                    }

                    TwistMode.PANIC -> {
                        fill(Color.RED)
                        brightness = .1f
                    }

                    TwistMode.AUDIO -> {
                        fill(GraphicsStuff.LIGHT_GREEN)
                        brightness = .07f
                    }

                    TwistMode.LIGHT -> {
                        fill(GOLDENROD)
                        brightness = .07f
                    }

                    TwistMode.OFF -> off()
                }
            }
        }

        // and now we check things
        return if (encoder.clicked) {
            when (twistMode) {
                TwistMode.NIGHT -> TheActions.HassActions.ALL_LIGHTS()
                TwistMode.PANIC -> TheActions.HassActions.ALL_LIGHTS()
                TwistMode.AUDIO -> currentMode = Mode.DAYTIME
                TwistMode.LIGHT -> twistMode = if (currentMode == Mode.NIGHT) TwistMode.NIGHT else TwistMode.PANIC
                TwistMode.OFF -> {
                    // nothing
                }
            }
            true
        } else if (encoder.moved) {
            // TODO lastcount vs this count
//            when (twistMode) {
//                TwistMode.AUDIO -> TheActions.MusicPlayActions.VOLUME_UP()
//                else -> { }
//            }
            true
        } else {
            false
        }
    }

    override fun stop() {
        if (::encoder.isInitialized) {
            encoder.pixel.off()
        }
    }
}
