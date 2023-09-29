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

import com.typesafe.config.ConfigFactory
import crackers.hassk.Constants.off
import crackers.hassk.Constants.on
import crackers.hassk.HAssKClient
import crackers.kobots.app.AppCommon
import crackers.kobots.mqtt.KobotsMQTT
import org.tinylog.Logger
import java.net.InetAddress

/**
 * What to do when a button is pressed.
 */
object TheActions {
    interface Action {
        fun execute()
    }

    internal val mqttClient: KobotsMQTT

    // remote control of this thing
    const val BBOARD_TOPIC = "kobots/buttonboard"

    enum class BBoardActions : Action {
        STOP, NEXT_FRONTBENCH;

        override fun execute() {
            when (this) {
                STOP -> AppCommon.applicationRunning = false
                NEXT_FRONTBENCH -> FrontBenchPicker.updateMenu()
            }
        }
    }

    init {
        with(ConfigFactory.load()) {
            mqttClient = KobotsMQTT(InetAddress.getLocalHost().hostName, getString("mqtt.broker")).apply {
                subscribe(BBOARD_TOPIC) { s: String ->
                    BBoardActions.valueOf(s.uppercase()).execute()
                }
            }
        }
    }

    internal enum class HassActions : Action {
        TOP, MORNING, OFFICE, BEDROOM, KITCHEN, TV, MOVIE, BEDTIME, LATE_NIGHT, NOT_ALL, OFFICE_FAN;

        override fun execute() = doAction(this)
    }

    private fun doAction(action: HassActions) {
        Logger.warn("Doing action {}", action)
        with(AppCommon.hasskClient) {
            when (action) {
                HassActions.TOP -> scene("top_button") turn on
                HassActions.MORNING -> scene("early_morning") turn on
                HassActions.OFFICE -> group("office_group") turn toggleOnLight("paper")
                HassActions.BEDROOM -> group("bedroom_group") turn toggleOnLight("shelf_lamp")
                HassActions.KITCHEN -> light("kitchen_lights") turn toggleOnLight("kitchen_lights")
                HassActions.TV -> scene("daytime_tv") turn on
                HassActions.MOVIE -> scene("movie_time") turn on
                HassActions.BEDTIME -> scene("bed_time") turn on
                HassActions.LATE_NIGHT -> scene("late_night") turn on
                HassActions.NOT_ALL -> group("not_bedroom_group") turn off
                HassActions.OFFICE_FAN -> switch("small_fan") turn toggleOnSwitch("small_fan")
            }
        }
    }

    const val GRIPOMATIC_TOPIC = "kobots/gripOMatic"

    enum class GripperActions : Action {
        PICKUP, RETURN, HOME, SAY_HI, STOP, EXCUSE_ME, SLEEP;

        override fun execute() = mqttClient.publish(GRIPOMATIC_TOPIC, name)
    }

    private fun HAssKClient.toggleOnSwitch(name: String) = if (switch(name).state().state == "off") on else off

    private fun HAssKClient.toggleOnLight(name: String) = if (light(name).state().state == "off") on else off

    // more for receiving messages than sending them
    internal enum class FrontBenchActions
}
