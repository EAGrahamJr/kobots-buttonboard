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

import crackers.kobots.buttonboard.Mode
import crackers.kobots.parts.app.io.NeoKeyMenu

/**
 * Handles what menu items are shown for the front "bench" (NeoKey) buttons.
 */
object FrontBenchPicker : BenchPicker<Mode>(0, 0) {
    override val menuSelections =
        mapOf(
            Mode.NIGHT to
                makeAMenu(
                    listOf(
                        NeoKeyMenu.NO_KEY,
                        HomeAssistantMenus.bedroomToggle,
                        NeoKeyMenu.NO_KEY,
                        stahp,
                    ),
                ),
            Mode.MORNING to
                makeAMenu(
                    listOf(
                        HomeAssistantMenus.fanControl,
                        NeoKeyMenu.NO_KEY,
                        NeoKeyMenu.NO_KEY,
                        stahp,
                    ),
                ),
            Mode.DAYTIME to
                makeAMenu(
                    listOf(
                        HomeAssistantMenus.fanControl,
                        NeoKeyMenu.NO_KEY,
//                        with(AppCommon.hasskClient) {
//                            (media("spotify") as HAssKClient.SpotifyPlayer).let { mp ->
//                                if (mp.state().state == "playing") {
//                                    MenuItem("Pse", icon = GraphicsStuff.NOT_NOTE, buttonColor = ORANGISH) {
//                                        TheActions.MusicPlayActions.PAUSE()
//                                    }
//                                }
//
// //                            if (mp.state)
//                                NeoKeyMenu.NO_KEY
//                            }
//                        },
                        HomeAssistantMenus.printerToggle,
                        stahp,
                    ),
                ),
            Mode.EVENING to
                makeAMenu(
                    listOf(
                        NeoKeyMenu.NO_KEY,
                        HomeAssistantMenus.bedroomToggle,
                        NeoKeyMenu.NO_KEY,
                        stahp,
                    ),
                ),
            Mode.AUDIO to
                makeAMenu(
                    listOf(
                        SystemMenus.deselectAudioItem,
                        NeoKeyMenu.NO_KEY,
                        NeoKeyMenu.NO_KEY,
                        NeoKeyMenu.NO_KEY,
                    ),
                ),
            Mode.DISABLED to
                makeAMenu(List(4) { i -> if (i == 3) stahp else NeoKeyMenu.NO_KEY }),
        )
}
