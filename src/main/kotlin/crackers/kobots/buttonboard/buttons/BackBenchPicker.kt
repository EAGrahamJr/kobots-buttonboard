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

import crackers.kobots.buttonboard.Mode
import crackers.kobots.parts.app.io.NeoKeyMenu

/**
 * Handles what menu items are shown for the back "bench" (NeoKey) buttons.
 */
object BackBenchPicker : BenchPicker<Mode>(1, 1) {
    override val menuSelections =
        mapOf(
            Mode.NIGHT to
                NeoKeyMenu(
                    keyHandler,
                    display,
                    listOf(
                        HomeAssistantMenus.nightOffFunction,
                        HomeAssistantMenus.fanControl,
                        HomeAssistantMenus.morningScene,
                        HomeAssistantMenus.daytimeScene,
                    ),
                ),
            Mode.MORNING to
                NeoKeyMenu(
                    keyHandler,
                    display,
                    listOf(
                        HomeAssistantMenus.daytimeScene,
                        HomeAssistantMenus.kitchenLights,
                        HomeAssistantMenus.morningScene,
                        NeoKeyMenu.NO_KEY,
                    ),
                ),
            Mode.DAYTIME to
                NeoKeyMenu(
                    keyHandler,
                    display,
                    listOf(
                        HomeAssistantMenus.daytimeScene,
                        HomeAssistantMenus.tvViewing,
                        HomeAssistantMenus.movieViewing,
                        HomeAssistantMenus.postTVScene,
                    ),
                ),
            Mode.EVENING to
                NeoKeyMenu(
                    keyHandler,
                    display,
                    listOf(
                        HomeAssistantMenus.bedtimeScene,
                        HomeAssistantMenus.lateNightScene,
                        HomeAssistantMenus.nightOffFunction,
                        HomeAssistantMenus.fanControl,
                    ),
                ),
// TODO we'll get back to this later
//            Mode.MANUAL to
//                NeoKeyMenu(
//                    keyHandler,
//                    display,
//                    manualLightMenu(),
//                ),
        )
}
