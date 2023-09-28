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
import crackers.kobots.buttonboard.TheActions.Actions
import crackers.kobots.buttonboard.TheActions.doAction
import crackers.kobots.parts.GOLDENROD
import crackers.kobots.parts.PURPLE
import crackers.kobots.parts.app.io.NeoKeyMenu
import crackers.kobots.parts.loadImage
import java.awt.Color
import java.awt.image.BufferedImage

internal enum class Images(val image: BufferedImage) {
    BED(loadImage("/bed.png")),
    EXIT(loadImage("/exit.png")),
    LIGHTBULB(loadImage("/lightbulb.png")),
    MOON(loadImage("/moon.png")),
    MOVIE(loadImage("/movie.png")),
    PRINTER(loadImage("/printer.png")),
    RESTAURANT(loadImage("/restaurant.png")),
    SUN(loadImage("/sun.png")),
    TV(loadImage("/tv.png")),
    FAN(loadImage("/fan.png"))
}

internal val ModeMenus = mapOf(
    Mode.NIGHT to NeoKeyMenu(
        handlerUno,
        displayUno,
        listOf(
            NeoKeyMenu.MenuItem(
                "Off",
                icon = Images.EXIT.image,
                buttonColor = Color.DARK_GRAY,
                action = { doAction(Actions.NOT_ALL) }
            ),
            NeoKeyMenu.MenuItem(
                "Top",
                icon = Images.LIGHTBULB.image,
                buttonColor = Color.GREEN,
                action = { doAction(Actions.TOP) }
            ),
            NeoKeyMenu.MenuItem(
                "Morn",
                icon = Images.SUN.image,
                buttonColor = GOLDENROD,
                action = { doAction(Actions.MORNING) }
            ),
            NeoKeyMenu.MenuItem(
                "Bed",
                icon = Images.BED.image,
                buttonColor = Color.PINK,
                action = { doAction(Actions.BEDROOM) }
            )
        )
    ),
    Mode.MORNING to NeoKeyMenu(
        handlerUno,
        displayUno,
        listOf(
            NeoKeyMenu.MenuItem(
                "Morn",
                icon = Images.SUN.image,
                buttonColor = GOLDENROD,
                action = { doAction(Actions.MORNING) }
            ),
            NeoKeyMenu.MenuItem(
                "Top",
                icon = Images.LIGHTBULB.image,
                buttonColor = Color.GREEN,
                action = { doAction(Actions.TOP) }
            ),
            NeoKeyMenu.MenuItem(
                "Kit",
                icon = Images.RESTAURANT.image,
                buttonColor = Color.CYAN,
                action = { doAction(Actions.KITCHEN) }
            ),
            NeoKeyMenu.MenuItem(
                "Fan",
                icon = Images.FAN.image,
                buttonColor = Color.BLUE,
                action = { doAction(Actions.OFFICE_FAN) }
            )
        )

    ),
    Mode.DAYTIME to
        NeoKeyMenu(
            handlerUno,
            displayUno,
            listOf(
                NeoKeyMenu.MenuItem(
                    "Top",
                    icon = Images.LIGHTBULB.image,
                    buttonColor = Color.GREEN,
                    action = { doAction(Actions.TOP) }
                ),
                NeoKeyMenu.MenuItem(
                    "TV",
                    icon = Images.TV.image,
                    buttonColor = PURPLE,
                    action = { doAction(Actions.TV) }
                ),
                NeoKeyMenu.MenuItem(
                    "Movie",
                    icon = Images.MOVIE.image,
                    buttonColor = Color.RED,
                    action = { doAction(Actions.MOVIE) }
                ),
                NeoKeyMenu.MenuItem(
                    "Fan",
                    icon = Images.FAN.image,
                    buttonColor = Color.BLUE,
                    action = { doAction(Actions.OFFICE_FAN) }
                )
            )

        ),
    Mode.EVENING to NeoKeyMenu(
        handlerUno,
        displayUno,
        listOf(
            NeoKeyMenu.MenuItem(
                "Bed",
                icon = Images.BED.image,
                buttonColor = Color.PINK,
                action = { doAction(Actions.BEDTIME) }
            ),
            NeoKeyMenu.MenuItem(
                "Late",
                icon = Images.MOON.image,
                buttonColor = Color.RED,
                action = { doAction(Actions.LATE_NIGHT) }
            ),
            NeoKeyMenu.MenuItem(
                "Off",
                icon = Images.EXIT.image,
                buttonColor = Color.DARK_GRAY,
                action = { doAction(Actions.NOT_ALL) }
            ),
            NeoKeyMenu.MenuItem(
                "Fan",
                icon = Images.FAN.image,
                buttonColor = Color.BLUE,
                action = { doAction(Actions.OFFICE_FAN) }
            )
        )
    )
)

val RobotMenu =
    NeoKeyMenu(
        handlerDos,
        displayDos,
        listOf(
            NeoKeyMenu.MenuItem(
                "Drops",
                icon = loadImage("/robot/symptoms.png"),
                buttonColor = Color.DARK_GRAY,
                action = { TheActions.GripperActions.PICKUP.send() }
            ),
            NeoKeyMenu.MenuItem(
                "Rtn",
                icon = loadImage("/robot/redo.png"),
                buttonColor = Color.GREEN,
                action = { TheActions.GripperActions.RETURN.send() }
            ),
            NeoKeyMenu.MenuItem(
                "Hi",
                icon = loadImage("/robot/hail.png"),
                buttonColor = Color.CYAN.darker(),
                action = { TheActions.GripperActions.SAY_HI.send() }
            ),
            NeoKeyMenu.MenuItem(
                "Exit",
                icon = loadImage("/robot/dangerous.png"),
                buttonColor = Color.RED,
                action = {
                    TheActions.GripperActions.STOP.send()
                    AppCommon.applicationRunning = false
                }
            )
        )
    )

/*


    const val EVENT_TOPIC = "kobots/events"
    // sequence completion comes from the gripomatic
    const val ROTO_PICKUP = "LocationPickup"
    const val ROTO_RETURN = "ReturnPickup"


private val smallMenu by lazy {
    listOf(
        NeoKeyMenu.MenuItem(
            "Home",
            icon = loadImage("/home.png")
        ) { armRequest(homeSequence) },
        NeoKeyMenu.MenuItem(
            "Say Hi",
            icon = loadImage("/hail.png"),
            buttonColor = Color.BLUE
        ) { armRequest(sayHi) },
        NeoKeyMenu.MenuItem(
            "Return",
            icon = loadImage("/redo.png"),
            buttonColor = GOLDENROD
        ) { publishToTopic(DA_TOPIC, returnRequested) },
        NeoKeyMenu.MenuItem(
            "Exit",
            icon = loadImage("/dangerous.png"),
            buttonColor = Color.RED
        ) {
            runFlag.set(false)
        }
    )
}
private val gripperMenu by lazy {
    listOf(
        NeoKeyMenu.MenuItem(
            "Home",
            icon = loadImage("/home.png")
        ) { armRequest(homeSequence) },
        NeoKeyMenu.MenuItem(
            "Say Hi",
            icon = loadImage("/hail.png"),
            buttonColor = Color.BLUE
        ) { armRequest(sayHi) },
        NeoKeyMenu.MenuItem(
            "Excuse Me",
            abbrev = "Sorry",
            icon = loadImage("/cancel.png"),
            buttonColor = PURPLE
        ) { armRequest(excuseMe) },
//    NeoKeyMenu.MenuItem("Manual", buttonColor = Color.ORANGE) { _manualMode.set(true) },
        NeoKeyMenu.MenuItem(
            "Lift It",
            icon = loadImage("/upload.png"),
            buttonColor = Color.GREEN
        ) { ServoMaticCommand.UP.send() },
        NeoKeyMenu.MenuItem(
            "Get It",
            icon = loadImage("/symptoms.png"),
            buttonColor = Color.CYAN
        ) { publishToTopic(DA_TOPIC, dropOffRequested) },
        NeoKeyMenu.MenuItem(
            "Return",
            icon = loadImage("/redo.png"),
            buttonColor = GOLDENROD
        ) { publishToTopic(DA_TOPIC, returnRequested) },
        NeoKeyMenu.MenuItem(
            "Exit",
            icon = loadImage("/dangerous.png"),
            buttonColor = Color.RED
        ) { runFlag.set(false) },
        NeoKeyMenu.MenuItem(
            "Sleep",
            icon = loadImage("/bed.png"),
            buttonColor = Color.BLUE.darker().darker()
        ) { armRequest(goToSleep) }
    )
}

 */
