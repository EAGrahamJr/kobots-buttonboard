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

import crackers.kobots.parts.GOLDENROD
import crackers.kobots.parts.ORANGISH
import crackers.kobots.parts.app.io.NeoKeyMenu.MenuItem
import crackers.kobots.parts.loadImage
import java.awt.Color

/*
 * Audio Menu items
 */
val audioPlay = MenuItem(
    "Play",
    icon = loadImage("/audio/music_note.png"),
    buttonColor = Color.GREEN,
    action = { TheActions.MopdiyActions.PLAY.execute() },
)
val audioPause = MenuItem(
    "Pause",
    icon = loadImage("/audio/music_off.png"),
    buttonColor = GOLDENROD,
    action = { TheActions.MopdiyActions.PAUSE.execute() },
)
val volumeUp = MenuItem(
    "Vol Up",
    abbrev = "V+",
    icon = loadImage("/audio/volume_up.png"),
    buttonColor = Color.GREEN,
    action = { TheActions.MopdiyActions.VOLUME_UP.execute() },
)
val volumeDown = MenuItem(
    "Vol Down",
    abbrev = "V-",
    icon = loadImage("/audio/volume_down.png"),
    buttonColor = ORANGISH,
    action = { TheActions.MopdiyActions.VOLUME_DOWN.execute() },
)
