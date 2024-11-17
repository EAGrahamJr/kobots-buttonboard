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

import crackers.hassk.Constants.off
import crackers.hassk.Constants.on
import crackers.hassk.HAssKClient
import crackers.kobots.app.AppCommon
import org.slf4j.LoggerFactory
import java.time.LocalTime

/**
 * What to do when a button is pressed.
 */
object TheActions {
    interface Action {
        operator fun invoke()
    }

    //    val mopidyKlient: MopidyKlient
    private val logger = LoggerFactory.getLogger("TheActions")

    // remote control of this thing
    const val BBOARD_TOPIC = "kobots/buttonboard"
    val TEN_THIRTY = LocalTime.of(20, 30, 0)

    enum class HassActions : Action {
        DAYTIME, // aka 'Top"
        MORNING,
        OFFICE,
        BEDROOM,
        KITCHEN,
        TV,
        MOVIE,
        BEDTIME,
        LATE_NIGHT,
        NOT_ALL,
        OFFICE_FAN,
        TFE,
        POST_TV,
        THING_PRINTER,
        ALL_LIGHTS,
        ;

        private fun HAssKClient.toggleOnSwitch(name: String) = if (switch(name).state().state == "off") on else off

        private fun HAssKClient.toggleOnLight(name: String) = if (light(name).state().state == "off") on else off

        override fun invoke() {
            val action = this
            logger.info("Doing action {}", this)

            with(AppCommon.hasskClient) {
                when (action) {
                    DAYTIME -> scene("top_button") turn on
                    MORNING -> scene("early_morning") turn on
                    OFFICE -> group("office_group") turn toggleOnLight("paper")
                    BEDROOM -> group("bedroom_group") turn toggleOnLight("shelf_lamp")
                    KITCHEN -> light("kitchen_lights") turn toggleOnLight("kitchen_lights")
                    TV -> scene("daytime_tv") turn on
                    MOVIE -> scene("movie_time") turn on
                    BEDTIME -> scene("bed_time") turn on
                    LATE_NIGHT -> scene("late_night") turn on
                    NOT_ALL -> {
                        if (LocalTime.now().isAfter(TEN_THIRTY)) {
                            currentMode = Mode.NIGHT
                        }
                        group("not_bedroom_group") turn off
                    }

                    OFFICE_FAN -> switch("small_fan") turn toggleOnSwitch("small_fan")
                    THING_PRINTER -> switch("tina_switch") turn toggleOnSwitch("tina_switch")
                    TFE -> scene("tfe") turn on
                    POST_TV -> scene("post_tv") turn on
                    ALL_LIGHTS -> light("all_lights") turn on
                }
            }
        }
    }

    enum class MusicPlayActions : Action {
        STOP,
        PLAY,
        PAUSE,
        NEXT,
        PREVIOUS,
        VOLUME_UP,
        VOLUME_DOWN,
        TOGGLE,
        MUTE,
        UNMUTE,
        SHUFFLE,
        REPEAT,
        REPEAT_OFF,
        REPEAT_ONE,
        ;

        override fun invoke() {
            logger.info("Doing action {}", this)
            val action = this
            with(AppCommon.hasskClient) {
                (media("spotify") as HAssKClient.SpotifyPlayer).let { mp ->
                    mp.currentPlayer = "edlap"
                    when (action) {
                        // STOP -> mp.pause() // stop() -- spotify don't do this
                        // PLAY -> mp.run { if (state().state != "playing") play() }
                        // PAUSE -> mp.run { if (state().state == "playing") pause() }
                        // NEXT -> mp.next()
                        // PREVIOUS -> mp.previous()
                        // VOLUME_UP -> mp.volumeUp()
                        // VOLUME_DOWN -> mp.volumeDown()
                        // TOGGLE -> mp.run { if (state().state != "playing") play() else pause() }
                        //                    MUTE -> mute()
                        //                    UNMUTE -> unmute()
                        //                    SHUFFLE -> shuffle()
                        //                    REPEAT -> repeat()
                        //                    REPEAT_OFF -> repeatOff()
                        //                    REPEAT_ONE -> repeatOne()
                        else -> logger.warn("Mopidy action {} not implemented", action)
                    }
                }
            }
        }
    }
}
