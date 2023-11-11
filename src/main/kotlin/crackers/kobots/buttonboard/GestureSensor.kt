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
import crackers.kobots.app.AppCommon.mqttClient
import crackers.kobots.devices.sensors.VCNL4040
import crackers.kobots.mqtt.KobotsMQTT.Companion.KOBOTS_EVENTS
import crackers.kobots.parts.elapsed
import crackers.kobots.parts.movement.SequenceExecutor.SequenceEvent
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

/**
 * Fire off events from a proximity sensor.
 */
object GestureSensor : AutoCloseable {
    private var lastStopCheck = false
    private lateinit var lastStopCheckTime: Instant
    private const val STOP_PROXIMITY = 20

    private var proxTriggered = false
    private const val PROXIMITY_THRESHOLD = 4
    private lateinit var proxTriggeredTime: Instant

    private val sensor = VCNL4040(i2cMultiplexer.getI2CDevice(7, VCNL4040.DEFAULT_I2C_ADDRESS))
        .apply {
            ambientLightEnabled = true
            proximityEnabled = true
        }

    private val logger = LoggerFactory.getLogger("GestureSensor")

    override fun close() {
        sensor.close()
    }

    fun whatAmIDoing() {
        val prox = sensor.proximity.toInt()
        val ambient = sensor.luminosity

        if (prox > STOP_PROXIMITY) checkStopGesture() else lastStopCheck = false

        // if over the threshold and the trigger has not fired, fire it
        if (prox > PROXIMITY_THRESHOLD && !proxTriggered) {
            proxTriggeredTime = Instant.now()
            fireEvent(true)
        }
        // otherwise clear after a second
        if (proxTriggered && prox < PROXIMITY_THRESHOLD && proxTriggeredTime.elapsed().toSeconds() > 1) {
            fireEvent(false)
        }
    }

    private fun fireEvent(b: Boolean) {
        proxTriggered = b
        val event = SequenceEvent("Proximity", "proxAlert", proxTriggered)
        mqttClient.publish(KOBOTS_EVENTS, JSONObject(event))
    }

    private val FOUR_SECONDS = Duration.ofSeconds(4)

    private fun checkStopGesture() {
        if (lastStopCheck) {
            val elapsed = lastStopCheckTime.elapsed()

            if (elapsed > FOUR_SECONDS) {
                logger.info("We're close, and we've been close for a while")
                AppCommon.applicationRunning = false
            }
        } else {
            lastStopCheckTime = Instant.now()
        }
        lastStopCheck = true
    }
}
