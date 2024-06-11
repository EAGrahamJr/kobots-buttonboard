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

package crackers.kobots.buttonboard.buttons

import crackers.kobots.buttonboard.TheActions.MusicPlayActions
import crackers.kobots.graphics.loadImage
import crackers.kobots.parts.GOLDENROD
import crackers.kobots.parts.ORANGISH
import crackers.kobots.parts.app.io.NeoKeyHandler
import crackers.kobots.parts.app.io.NeoKeyMenu
import crackers.kobots.parts.app.io.NeoKeyMenu.MenuItem
import crackers.kobots.parts.app.io.SmallMenuDisplay
import java.awt.Color
import java.awt.image.BufferedImage

object AudioItems {
    enum class AudioImages(val image: BufferedImage) {
        PLAY(loadImage("/audio/music_note.png")),
        PAUSE(loadImage("/audio/music_off.png")),
        VOLUME_UP(loadImage("/audio/volume_up.png")),
        VOLUME_DOWN(loadImage("/audio/volume_down.png")),
    }

    /*
     * Audio Menu items
     */
    val audioPlay =
        MenuItem("Play", icon = AudioImages.PLAY.image, buttonColor = Color.GREEN) { MusicPlayActions.PLAY() }

    val audioPause =
        MenuItem("Pause", icon = AudioImages.PAUSE.image, buttonColor = GOLDENROD) { MusicPlayActions.PAUSE() }

    val volumeUp =
        MenuItem("Vol Up", abbrev = "V+", icon = AudioImages.VOLUME_UP.image, buttonColor = Color.GREEN) {
            MusicPlayActions.VOLUME_UP()
        }

    val volumeDown =
        MenuItem("Vol Down", abbrev = "V-", icon = AudioImages.VOLUME_DOWN.image, buttonColor = ORANGISH) {
            MusicPlayActions.VOLUME_DOWN()
        }

    fun audioPlayMenu(
        handler: NeoKeyHandler,
        display: SmallMenuDisplay,
    ) = NeoKeyMenu(
        handler,
        display,
        listOf(
            audioPlay,
            audioPause,
            volumeUp,
            volumeDown,
        ),
    )
}
