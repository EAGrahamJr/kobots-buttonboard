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
import crackers.kobots.app.AppCommon.whileRunning
import crackers.kobots.devices.lighting.NeoPixel
import crackers.kobots.devices.lighting.WS2811
import crackers.kobots.mqtt.KobotsMQTT
import crackers.kobots.mqtt.homeassistant.KobotRGBLight
import crackers.kobots.mqtt.homeassistant.SinglePixelLightController
import crackers.kobots.parts.scheduleWithFixedDelay
import org.slf4j.LoggerFactory
import java.awt.Color
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates
import kotlin.time.Duration.Companion.seconds

/**
 * Blinken lights.
 */
object RosetteStatus {
    private val lastCheckIn = ConcurrentHashMap<String, ZonedDateTime>()
    private val hostList = listOf("brainz", "marvin", "useless", "zeke", "ringo", "murphy", "psyche")
    private val logger by lazy { LoggerFactory.getLogger("RosetteStatus") }
    internal val goToSleep = AtomicBoolean(false)
    lateinit var rosette: NeoPixel
    var rosetteOffset by Delegates.notNull<Int>()

    /**
     * Listens for `KOBOTS_ALIVE` messages and tracks the last time a message was received from each host.
     *
     * This is not using the "built-in" MQTT listener because we're also checking for "up" statuses.
     */
    internal fun manageAliveChecks(
        statusPixels: NeoPixel,
        mqtt: KobotsMQTT,
        pixelOffset: Int = 0,
    ) {
        rosette = statusPixels
        rosetteOffset = pixelOffset

        // "reserve" pixel 7 and register it as a KobotLight for Home Assistant
        rosette[pixelOffset + 7] = Color.BLACK
        KobotRGBLight(
            "crazy_light",
            SinglePixelLightController(rosette, pixelOffset + 7),
            "My Little Pixel",
            deviceIdentifier = haDevice,
        )
            .start()

        // store everybody's last time
        mqtt.subscribe(KobotsMQTT.KOBOTS_ALIVE) { s: String -> lastCheckIn[s] = ZonedDateTime.now() }

        // check for dead kobots
        AppCommon.executor.scheduleWithFixedDelay(15.seconds, 15.seconds) {
            whileRunning {
                if (goToSleep.get()) {
                    (0 until hostList.size).forEach {
                        rosette[it + rosetteOffset] = WS2811.PixelColor(Color.BLACK, brightness = 0.0f)
                    }
                } else {
                    displayAliveCheck()
                }
            }
        }
    }

    private fun displayAliveCheck() {
        val now = ZonedDateTime.now()
        lastCheckIn.forEach { (host, lastSeenAt) ->
            val lastGasp = Duration.between(lastSeenAt, now).seconds
            val pixelNumber = hostList.indexOf(host)
            if (pixelNumber < 0) {
                logger.warn("Unknown host $host")
            } else {
                rosette[pixelNumber + rosetteOffset] =
                    when {
                        goToSleep.get() -> WS2811.PixelColor(Color.BLACK, brightness = 0.0f)
                        lastGasp < 60 -> WS2811.PixelColor(Color.GREEN, brightness = 0.005f)
                        lastGasp < 120 -> WS2811.PixelColor(Color.YELLOW, brightness = 0.01f)
                        else -> {
                            logger.error("$host is not responding")
                            WS2811.PixelColor(Color.RED, brightness = 0.02f)
                        }
                    }
            }
        }
        clearAndShow()
    }

    /**
     * "unknown" hosts get a blue pixel, then we show all the things
     */
    private fun clearAndShow() {
        hostList.forEachIndexed { i, host ->
            if (!lastCheckIn.containsKey(host)) {
                rosette[i + rosetteOffset] = WS2811.PixelColor(Color.BLUE, brightness = 0.005f)
            }
        }
        rosette.show()
    }

    /**
     * Clears the statuses.
     */
    fun reset() {
        lastCheckIn.clear()
        clearAndShow()
    }
}
