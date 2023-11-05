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

import crackers.kobots.app.AppCommon
import crackers.kobots.devices.sensors.VCNL4040
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

/**
 * TODO currently using TimeOfFlight sensor, but hooe to actually use a gesture sensor
 */
object GestureSensor : AutoCloseable {
    private var wereWeCloseLastTime = false
    private lateinit var whenWeWereCloseLastTime: Instant

    private val sensor = VCNL4040(i2cMultiplexer.getI2CDevice(7, VCNL4040.DEFAULT_I2C_ADDRESS))
        .apply {
            ambientLightEnabled = true
            proximityEnabled = true
        }
    override fun close() {
        sensor.close()
    }

    fun whatAmIDoing() {
        wereWeCloseLastTime = isItClose().also { yikes ->
            if (yikes) {
                if (wereWeCloseLastTime) {
                    if (Duration.between(whenWeWereCloseLastTime, Instant.now()) > Duration.ofSeconds(4)) {
                        LoggerFactory.getLogger("GestureSensor").info("We're close, and we've been close for a while")
                        AppCommon.applicationRunning = false
                    }
                } else {
                    whenWeWereCloseLastTime = Instant.now()
                }
            }
        }
    }

    fun isItClose(distance: Int = 19) = sensor.proximity > distance
}
