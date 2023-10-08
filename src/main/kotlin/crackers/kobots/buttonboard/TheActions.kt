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
import crackers.mopidykontrol.MopidyKlient
import org.tinylog.Logger
import java.net.InetAddress

/**
 * What to do when a button is pressed.
 */
object TheActions {
    interface Action {
        fun execute()
    }

    val mqttClient: KobotsMQTT
    val mopidyKlient: MopidyKlient

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
            mopidyKlient = MopidyKlient(getString("mopidy.host"), getInt("mopidy.port"))
        }
    }

    enum class HassActions : Action {
        TOP, MORNING, OFFICE, BEDROOM, KITCHEN, TV, MOVIE, BEDTIME, LATE_NIGHT, NOT_ALL, OFFICE_FAN;

        private fun HAssKClient.toggleOnSwitch(name: String) = if (switch(name).state().state == "off") on else off

        private fun HAssKClient.toggleOnLight(name: String) = if (light(name).state().state == "off") on else off

        override fun execute() {
            val action = this
            Logger.warn("Doing action {}", this)

            with(AppCommon.hasskClient) {
                when (action) {
                    TOP -> scene("top_button") turn on
                    MORNING -> scene("early_morning") turn on
                    OFFICE -> group("office_group") turn toggleOnLight("paper")
                    BEDROOM -> group("bedroom_group") turn toggleOnLight("shelf_lamp")
                    KITCHEN -> light("kitchen_lights") turn toggleOnLight("kitchen_lights")
                    TV -> scene("daytime_tv") turn on
                    MOVIE -> scene("movie_time") turn on
                    BEDTIME -> scene("bed_time") turn on
                    LATE_NIGHT -> scene("late_night") turn on
                    NOT_ALL -> group("not_bedroom_group") turn off
                    OFFICE_FAN -> switch("small_fan") turn toggleOnSwitch("small_fan")
                }
            }
        }
    }

    enum class GripperActions : Action {
        PICKUP, RETURN, HOME, SAY_HI, STOP, EXCUSE_ME, SLEEP, FLASHLIGHT;

        val GRIPOMATIC_TOPIC = "kobots/gripOMatic"

        override fun execute() = mqttClient.publish(GRIPOMATIC_TOPIC, name)
    }

    enum class MopdiyActions : Action {
        STOP, PLAY, PAUSE, NEXT, PREVIOUS, VOLUME_UP, VOLUME_DOWN, MUTE, UNMUTE, SHUFFLE, REPEAT, REPEAT_OFF, REPEAT_ONE;

        override fun execute() {
            val action = this
            with(mopidyKlient) {
                when (action) {
                    STOP -> stop()
                    PLAY -> play()
                    PAUSE -> pause()
                    NEXT -> next()
                    PREVIOUS -> previous()
//                    VOLUME_UP -> volumeUp()
//                    VOLUME_DOWN -> volumeDown()
//                    MUTE -> mute()
//                    UNMUTE -> unmute()
//                    SHUFFLE -> shuffle()
//                    REPEAT -> repeat()
//                    REPEAT_OFF -> repeatOff()
//                    REPEAT_ONE -> repeatOne()
                    else -> Logger.warn("Mopidy action {} not implemented", action)
                }
            }
        }
    }

    // more for receiving messages than sending them
    enum class FrontBenchActions
}
