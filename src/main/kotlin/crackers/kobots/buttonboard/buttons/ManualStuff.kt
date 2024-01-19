package crackers.kobots.buttonboard.buttons

import crackers.kobots.app.AppCommon
import crackers.kobots.buttonboard.Mode
import crackers.kobots.buttonboard.buttons.BackBenchPicker.keyHandler
import crackers.kobots.buttonboard.buttons.HomeAssistantMenus.IMAGE_BULB
import crackers.kobots.buttonboard.currentMode
import crackers.kobots.parts.ORANGISH
import crackers.kobots.parts.app.io.NeoKeyMenu
import org.json.JSONObject
import java.awt.Color
import java.util.concurrent.atomic.AtomicInteger

/**
 * TODO fill this in
 */
object ManualStuff {
    private const val NO_TOGGLE = -1
    private val toggleActive = AtomicInteger(NO_TOGGLE)

    private class RotoAction(val entityId: String, val currentBrightness: Int = 0) : () -> Unit {
        override fun invoke() {
            RotoRegulator.mangageLight(entityId, currentBrightness)
        }
    }

    private val blinker = BenchPicker.Blinker(keyHandler.keyboard.pixels, ORANGISH)

    /**
     * Builds list of selectable lights to control.
     */
    private fun manualLightMenu(): List<NeoKeyMenu.MenuItem> {
        val itemList = mutableListOf<NeoKeyMenu.MenuItem>()
        itemList +=
            NeoKeyMenu.MenuItem(
                "Back",
                icon = KobotsMenus.RobotImages.RETURN.image,
                buttonColor = HomeAssistantMenus.DARK_CYAN,
            ) {
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
                    NeoKeyMenu.MenuItem(
                        name = name.take(4),
                        icon = IMAGE_BULB,
                        buttonColor = buttonColor,
                        action = RotoAction(id, brightness),
                    )
                itemList += item
            }
        }
        itemList += NeoKeyMenu.NO_KEY

        return itemList
    }

    fun multiTaskingButtons(): Boolean =
//        currentMenu.execute().firstOrNull()?.let {
//            val (button, menuItem) = it
//            if (currentMode == Mode.MANUAL) manualButtonPress(button, menuItem) else menuItem.action()
//            true
//        } ?: false
        TODO()

    private fun manualButtonPress(
        button: Int,
        menuItem: NeoKeyMenu.MenuItem,
    ) {
        // assume we're accepting the button
        if (toggleActive.compareAndSet(NO_TOGGLE, button)) {
            // if it's a "roto action", turn on blinky and set up the rotator
            menuItem.action.run {
                if (this is RotoAction) {
                    val buttonColor = if (entityId.contains("group")) Color.BLUE else Color.GREEN
                    blinker.start(button, buttonColor)
                } else { // otherwise, it's a "normal" thing, so clear the toggle and execute
                    blinker.stop()
                    toggleActive.set(NO_TOGGLE)
                }
                invoke()
            }
        } else if (toggleActive.compareAndSet(button, NO_TOGGLE)) { // or it's the active button and we're toggling off
            // stop blinky
            blinker.stop()
            RotoRegulator.mangageLight(null, 0)
        }
        // ignored
    }
}
