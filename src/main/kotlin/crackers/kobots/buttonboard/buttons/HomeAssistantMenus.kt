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

package crackers.kobots.buttonboard.buttons

import crackers.kobots.app.AppCommon
import crackers.kobots.buttonboard.GraphicsStuff.DARK_CYAN
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_BED
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_BULB
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_EAR
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_EXIT
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_FAN
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_HOTEL
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_KITCHEN
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_LIGHTGROUP
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_MOON
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_MOVIE
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_PRINTER
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_RELAX
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_SKULL
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_SUN
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_TV
import crackers.kobots.buttonboard.GraphicsStuff.LIGHT_GREEN
import crackers.kobots.buttonboard.GraphicsStuff.MEDIA_NEXT
import crackers.kobots.buttonboard.GraphicsStuff.MEDIA_PAUSE
import crackers.kobots.buttonboard.GraphicsStuff.MEDIA_PLAY
import crackers.kobots.buttonboard.GraphicsStuff.MEDIA_PREV
import crackers.kobots.buttonboard.TheActions.HassActions
import crackers.kobots.buttonboard.TheActions.MusicPlayActions
import crackers.kobots.parts.GOLDENROD
import crackers.kobots.parts.ORANGISH
import crackers.kobots.parts.PURPLE
import crackers.kobots.parts.app.io.NeoKeyMenu.MenuItem
import java.awt.Color

/**
 * Ibid
 */
object HomeAssistantMenus {
    val nightOffFunction =
        MenuItem("Off", icon = IMAGE_EXIT, buttonColor = GOLDENROD) {
            HassActions.NOT_ALL()
            MusicPlayActions.STOP()
        }

    val fanControl =
        MenuItem("Fan", icon = IMAGE_FAN, buttonColor = Color.BLUE) {
            HassActions.OFFICE_FAN()
            with(AppCommon.hasskClient) {
                (if (switch("small_fan").state().state == "off") MusicPlayActions.PLAY else MusicPlayActions.STOP)()
            }
        }

    val morningScene =
        MenuItem("Morn", icon = IMAGE_SUN, buttonColor = GOLDENROD) {
            HassActions.MORNING()
            MusicPlayActions.PLAY()
        }

    val daytimeScene = MenuItem("Day", icon = IMAGE_BULB, buttonColor = Color.GREEN) { HassActions.DAYTIME() }
    val kitchenLights = MenuItem("Kit", icon = IMAGE_KITCHEN, buttonColor = Color.CYAN) { HassActions.KITCHEN() }

    val tvViewing =
        MenuItem("TV", icon = IMAGE_TV, buttonColor = PURPLE) {
            HassActions.TV()
            MusicPlayActions.STOP()
        }

    val movieViewing =
        MenuItem("Movie", icon = IMAGE_MOVIE, buttonColor = Color.RED.darker()) {
            HassActions.MOVIE()
            MusicPlayActions.STOP()
        }

    val printerToggle = MenuItem("Pntr", icon = IMAGE_PRINTER, buttonColor = GOLDENROD) { HassActions.THING_PRINTER() }
    val bedtimeScene = MenuItem("BTime", icon = IMAGE_HOTEL, buttonColor = Color.PINK) { HassActions.BEDTIME() }
    val lateNightScene = MenuItem("Late", icon = IMAGE_MOON, buttonColor = DARK_CYAN) { HassActions.LATE_NIGHT() }
    val tfeScene = MenuItem("TFE", icon = IMAGE_SKULL, buttonColor = Color.RED.darker()) { HassActions.TFE() }
    val bedroomToggle = MenuItem("BRm", icon = IMAGE_BED, buttonColor = Color.PINK) { HassActions.BEDROOM() }
    val allOn = MenuItem("All", icon = IMAGE_LIGHTGROUP, buttonColor = LIGHT_GREEN) { HassActions.ALL_LIGHTS() }
    val whiteNoiseToggle = MenuItem("EAR", icon = IMAGE_EAR, buttonColor = ORANGISH) { MusicPlayActions.TOGGLE() }
    val postTVScene = MenuItem("Post", icon = IMAGE_RELAX, buttonColor = Color.YELLOW) { HassActions.POST_TV() }

    val mediaPrevious = MenuItem("Prev", icon = MEDIA_PREV, buttonColor = Color.CYAN) { MusicPlayActions.PREVIOUS() }
    val mediaPause = MenuItem("Pause", icon = MEDIA_PAUSE, buttonColor = GOLDENROD) { MusicPlayActions.PAUSE() }
    val mediaPlay = MenuItem("Play", icon = MEDIA_PLAY, buttonColor = Color.GREEN) { MusicPlayActions.PLAY() }
    val mediaNext = MenuItem("Next", icon = MEDIA_NEXT, buttonColor = Color.CYAN) { MusicPlayActions.NEXT() }
}
