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

import crackers.kobots.devices.sensors.VCNL4040
import crackers.kobots.mqtt.homeassistant.KobotAnalogSensor
import crackers.kobots.mqtt.homeassistant.KobotBinarySensor
import crackers.kobots.parts.elapsed
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

/**
 * Fire off events from a proximity sensor.
 */
object GestureSensor : AutoCloseable {
    private lateinit var lastAmbientSent: Instant

    private var proxTriggered = false
    private const val PROXIMITY_THRESHOLD = 4
    private lateinit var proxTriggeredTime: Instant

    private val proxSensor =
        object : KobotBinarySensor(
            "proximity_alert",
            "Proximity",
            haDevice,
            deviceClass = KobotBinarySensor.Companion.BinaryDevice.OCCUPANCY,
        ) {
            override val icon = "mdi:alert"
        }

    private val ambientSensor =
        object : KobotAnalogSensor(
            "ambient_light",
            "Luminosity",
            haDevice,
            deviceClass =
            KobotAnalogSensor.Companion.AnalogDevice
                .ILLUMINANCE,
            unitOfMeasurement = "lumens",
        ) {
            override val icon = "mdi:lightbulb-alert"
        }

    private val sensor =
        VCNL4040(i2cMultiplexer.getI2CDevice(7, VCNL4040.DEFAULT_I2C_ADDRESS))
            .apply {
                ambientLightEnabled = true
                proximityEnabled = true
            }

    private val logger = LoggerFactory.getLogger("GestureSensor")

    override fun close() {
        sensor.close()
    }

    fun whatAmIDoing() {
        if (!::lastAmbientSent.isInitialized) {
            lastAmbientSent = Instant.EPOCH
            proxSensor.start()
            ambientSensor.start()
        }
        val prox = sensor.proximity.toInt()

        // if over the threshold and the trigger has not fired, fire it
        if (prox > PROXIMITY_THRESHOLD && !proxTriggered) {
            proxTriggeredTime = Instant.now()
            fireEvent(true)
        }
        // otherwise clear after a second
        if (proxTriggered && prox < PROXIMITY_THRESHOLD && proxTriggeredTime.elapsed().toSeconds() > 1) {
            fireEvent(false)
        }

        // time to send the ambient light?
        if (lastAmbientSent.elapsed() > Duration.ofMinutes(1)) {
            lastAmbientSent = Instant.now()
            ambientSensor.currentState = sensor.luminosity.toString()
        }
    }

    private fun fireEvent(b: Boolean) {
        proxTriggered = b
        proxSensor.currentState = b
    }
}
