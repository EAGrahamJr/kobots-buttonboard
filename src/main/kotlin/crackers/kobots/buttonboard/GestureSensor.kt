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

import crackers.kobots.buttonboard.TheActions.mopidyKlient
import crackers.kobots.devices.sensors.VL6180X
import crackers.kobots.parts.app.KobotSleep
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

/**
 * TODO currently using TimeOfFlight sensor, but hooe to actually use a gesture sensor
 */
object GestureSensor : AutoCloseable {
    private val sensor = VL6180X()
    override fun close() {
        sensor.close()
    }

    private val mode = AtomicBoolean(false)
    var volumeMode: Boolean
        get() = mode.get()
        set(value) {
            mode.set(value)
        }

    fun whatAmIDoing() {
        if (volumeMode) {
            // TODO how do you "lock" it?
            KobotSleep.millis(100)
            mopidyKlient.volume = min(sensor.range / 2.0, 100.0).toInt()
        } else {
            wereWeCloseLastTime = isItClose().also {
                if (it && !wereWeCloseLastTime) {
                    FrontBenchPicker.updateMenu()
//                    volumeMode = (FrontBenchPicker.currentMenu == FrontBenchPicker.audioPlayMenu)
                }
            }
        }
    }

    private var wereWeCloseLastTime = false
    fun isItClose(distance: Int = 50) = sensor.range < distance
}
