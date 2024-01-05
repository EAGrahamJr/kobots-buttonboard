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

import crackers.kobots.app.AppCommon
import crackers.kobots.buttonboard.Mode
import crackers.kobots.buttonboard.TheActions.HassActions
import crackers.kobots.buttonboard.TheActions.MopdiyActions
import crackers.kobots.buttonboard.buttons.BenchPicker.Companion.HAImages
import crackers.kobots.buttonboard.buttons.BenchPicker.Companion.RobotImages
import crackers.kobots.buttonboard.currentMode
import crackers.kobots.parts.GOLDENROD
import crackers.kobots.parts.ORANGISH
import crackers.kobots.parts.PURPLE
import crackers.kobots.parts.app.io.NeoKeyMenu
import crackers.kobots.parts.app.io.NeoKeyMenu.Companion.NO_KEY
import crackers.kobots.parts.app.io.NeoKeyMenu.MenuItem
import org.json.JSONObject
import java.awt.Color
import java.util.concurrent.atomic.AtomicInteger

/**
 * Handles what menu items are shown for the back "bench" (NeoKey) buttons.
 */
object BackBenchPicker : BenchPicker<Mode>(1, 1) {
    private const val NO_TOGGLE = -1
    private val toggleActive = AtomicInteger(NO_TOGGLE)

    val topMenuItem = MenuItem("Top", icon = HAImages.LIGHTBULB.image, buttonColor = Color.GREEN) { HassActions.TOP() }

    val morningMenuItem =
        MenuItem("Morn", icon = HAImages.SUN.image, buttonColor = GOLDENROD) {
            HassActions.MORNING()
            MopdiyActions.PLAY()
        }

    val fanControl =
        MenuItem("Fan", icon = HAImages.FAN.image, buttonColor = Color.BLUE) {
            HassActions.OFFICE_FAN()
            with(AppCommon.hasskClient) {
                (if (switch("small_fan").state().state == "off") MopdiyActions.PLAY else MopdiyActions.STOP)()
            }
        }

    val notAllOff =
        MenuItem("Off", icon = HAImages.EXIT.image, buttonColor = Color.DARK_GRAY) {
            HassActions.NOT_ALL()
            MopdiyActions.STOP()
        }

    val bedroom = MenuItem("Bed", icon = HAImages.BED.image, buttonColor = Color.PINK) { HassActions.BEDROOM() }

    val stahp =
        MenuItem("Stop", icon = CANCEL_ICON, buttonColor = Color.ORANGE) {
            AppCommon.applicationRunning = false
        }
    override val menuSelections =
        mapOf(
            Mode.NIGHT to
                NeoKeyMenu(
                    keyHandler,
                    display,
                    listOf(
                        notAllOff,
                        morningMenuItem,
                        bedroom,
                        topMenuItem,
                    ),
                ),
            Mode.MORNING to
                NeoKeyMenu(
                    keyHandler,
                    display,
                    listOf(
                        topMenuItem,
                        morningMenuItem,
                        MenuItem(
                            "Kit",
                            icon = HAImages.RESTAURANT.image,
                            buttonColor = Color.CYAN,
                        ) { HassActions.KITCHEN() },
                        fanControl,
                        NO_KEY,
                        stahp,
                    ),
                ),
            Mode.DAYTIME to
                NeoKeyMenu(
                    keyHandler,
                    display,
                    listOf(
                        topMenuItem,
                        MenuItem("Dim", buttonColor = GOLDENROD) { HassActions.TV() },
                        bedroom,
                        MenuItem("TV", icon = HAImages.TV.image, buttonColor = PURPLE) {
                            HassActions.TV()
                            MopdiyActions.STOP()
                        },
                        MenuItem("Movie", icon = HAImages.MOVIE.image, buttonColor = Color.RED.darker()) {
                            HassActions.MOVIE()
                            MopdiyActions.STOP()
                        },
                        fanControl,
                        NO_KEY,
                        MenuItem("Manual", icon = RobotImages.STOP_IT.image, buttonColor = DARK_CYAN) {
                            currentMode = Mode.MANUAL
                        },
                        stahp,
                    ),
                ),
            Mode.EVENING to
                NeoKeyMenu(
                    keyHandler,
                    display,
                    listOf(
                        MenuItem(
                            "Bed",
                            icon = HAImages.BED.image,
                            buttonColor = Color.PINK,
                        ) { HassActions.BEDTIME() },
                        MenuItem(
                            "Late",
                            icon = HAImages.MOON.image,
                            buttonColor = Color.RED,
                        ) { HassActions.LATE_NIGHT() },
                        notAllOff,
                        fanControl,
                    ),
                ),
            Mode.MANUAL to
                NeoKeyMenu(
                    keyHandler,
                    display,
                    manualLightMenu(),
                ),
        )

    private class RotoAction(val entityId: String, val currentBrightness: Int = 0) : () -> Unit {
        override fun invoke() {
            RotoRegulator.mangageLight(entityId, currentBrightness)
        }
    }

    private val blinker = Blinker(keyHandler.keyboard.pixels, ORANGISH)

    /**
     * Builds list of selectable lights to control.
     */
    private fun manualLightMenu(): List<MenuItem> {
        val itemList = mutableListOf<MenuItem>()
        itemList +=
            MenuItem("Back", icon = RobotImages.RETURN.image, buttonColor = DARK_CYAN) {
                blinker.stop()
                RotoRegulator.mangageLight(null, 0)
                currentMode = Mode.DAYTIME
            }

        with(AppCommon.hasskClient) {
            states("light").forEach {
                val id = it.entityId
                val buttonColor = if (id.contains("group")) Color.BLUE else Color.GREEN
                // if (state) Color.GREEN else Color.DARK_GRAY

                val attributes = JSONObject(it.attributes)
                val name = attributes.optString("friendly_name")
                val state = it.state == "on"
                val brightness = attributes.optInt("brightness", 0)
                val item =
                    MenuItem(
                        name = name.take(4),
                        icon = HAImages.LIGHTBULB.image,
                        buttonColor = buttonColor,
                        action = RotoAction(id, brightness),
                    )
                itemList += item
            }
        }
        itemList += NO_KEY

        return itemList
    }

    fun multiTaskingButtons() =
        currentMenu.execute().firstOrNull()?.let {
            val (button, menuItem) = it
            if (currentMode == Mode.MANUAL) manualButtonPress(button, menuItem) else menuItem.action()
            true
        } ?: false

    private fun manualButtonPress(
        button: Int,
        menuItem: MenuItem,
    ) {
        // assume we're accepting the button
        if (toggleActive.compareAndSet(NO_TOGGLE, button)) {
            // if it's a "roto action", turn on blinky and set up the rotator
            menuItem.action.run {
                if (this is RotoAction) {
                    val buttonColor = if (entityId.contains("group")) Color.BLUE else Color.GREEN
                    blinker.start(button, buttonColor)
                }
                // otherwise, it's a "normal" thing, so clear the toggle and execute
                else {
                    blinker.stop()
                    toggleActive.set(NO_TOGGLE)
                }
                invoke()
            }
        }
        // or it's the active button and we're toggling off
        else if (toggleActive.compareAndSet(button, NO_TOGGLE)) {
            // stop blinky
            blinker.stop()
            RotoRegulator.mangageLight(null, 0)
        }
        // ignored
    }
}
