/*
 * Copyright 2022-2026 by E. A. Graham, Jr.
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
import java.awt.image.BufferedImage

/**
 * TODO fill this in
 */
object GraphicsStuff {
    val CANCEL_ICON: BufferedImage = loadImage("/cancel.png")
    val DARK_CYAN: Color = Color.CYAN.darker()
    val OLIVE_GREEN = Color(.39f, .38f, .23f) // blech
    val LIGHT_GREEN = Color(125, 255, 0)

    val IMAGE_BED: BufferedImage = loadImage("/bed.png")
    val IMAGE_EXIT: BufferedImage = loadImage("/exit.png")
    val IMAGE_BULB: BufferedImage = loadImage("/lightbulb.png")
    val IMAGE_HOTEL: BufferedImage = loadImage("/hotel.png")
    val IMAGE_MOON: BufferedImage = loadImage("/moon.png")
    val IMAGE_MOVIE: BufferedImage = loadImage("/movie.png")
    val IMAGE_KITCHEN: BufferedImage = loadImage("/restaurant.png")
    val IMAGE_SUN: BufferedImage = loadImage("/sun.png")
    val IMAGE_TV: BufferedImage = loadImage("/tv.png")
    val IMAGE_FAN: BufferedImage = loadImage("/fan.png")
    val IMAGE_SKULL: BufferedImage = loadImage("/skull.png")
    val IMAGE_LIGHTGROUP: BufferedImage = loadImage("/light_group.png")
    val IMAGE_EAR: BufferedImage = loadImage("/hearing.png")
    val IMAGE_RELAX: BufferedImage = loadImage("/relax.png")
    val IMAGE_PRINTER: BufferedImage = loadImage("/print.png")

    val NOTE: BufferedImage = loadImage("/media/music_note.png")
    val NOT_NOTE: BufferedImage = loadImage("/media/music_off.png")
    val MEDIA_PREV: BufferedImage = loadImage("/media/skip_previous.png")
    val MEDIA_PAUSE: BufferedImage = loadImage("/media/pause.png")
    val MEDIA_PLAY: BufferedImage = loadImage("/media/play_arrow.png")
    val MEDIA_NEXT: BufferedImage = loadImage("/media/skip_next.png")
}
