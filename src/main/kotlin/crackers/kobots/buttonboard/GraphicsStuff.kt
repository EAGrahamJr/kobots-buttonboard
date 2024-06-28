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

import crackers.kobots.graphics.loadImage
import java.awt.Color

/**
 * TODO fill this in
 */
object GraphicsStuff {
    val CANCEL_ICON = loadImage("/cancel.png")
    val DARK_CYAN = Color.CYAN.darker()
    val OLIVE_GREEN = Color(.39f, .38f, .23f) // blech
    val LIGHT_GREEN = Color(125, 255, 0)

    val IMAGE_BED = loadImage("/bed.png")
    val IMAGE_EXIT = loadImage("/exit.png")
    val IMAGE_BULB = loadImage("/lightbulb.png")
    val IMAGE_HOTEL = loadImage("/hotel.png")
    val IMAGE_MOON = loadImage("/moon.png")
    val IMAGE_MOVIE = loadImage("/movie.png")
    val IMAGE_KITCHEN = loadImage("/restaurant.png")
    val IMAGE_SUN = loadImage("/sun.png")
    val IMAGE_TV = loadImage("/tv.png")
    val IMAGE_FAN = loadImage("/fan.png")
    val IMAGE_SKULL = loadImage("/skull.png")
    val IMAGE_LIGHTGROUP = loadImage("/light_group.png")
    val IMAGE_EAR = loadImage("/hearing.png")
    val IMAGE_RELAX = loadImage("/relax.png")
    val IMAGE_PRINTER = loadImage("/print.png")

    val NOTE = loadImage("/media/music_note.png")
    val NOT_NOTE = loadImage("/media/music_off.png")
    val MEDIA_PREV = loadImage("/media/skip_previous.png")
    val MEDIA_PAUSE = loadImage("/media/pause.png")
    val MEDIA_PLAY = loadImage("/media/play_arrow.png")
    val MEDIA_NEXT = loadImage("/media/skip_next.png")
}
