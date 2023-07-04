package crackers.kobots.buttonboard

import com.typesafe.config.ConfigFactory
import crackers.hassk.Constants.off
import crackers.hassk.Constants.on
import crackers.hassk.HAssKClient
import org.tinylog.Logger

/**
 * What to do when a button is pressed.
 */
object TheActions {
    internal val hasskClient = with(ConfigFactory.load()) {
        HAssKClient(getString("ha.token"), getString("ha.server"), getInt("ha.port"))
    }

    internal enum class Actions {
        TOP, MORNING, OFFICE, BEDROOM, KITCHEN, TV, MOVIE, BEDTIME, LATE_NIGHT, NOT_ALL
    }

    /**
     * Do this serially because it takes time.
     */
    internal fun doStuff(button: Int, mode: Mode) {
        mode.actions[button].let { doAction(it) }
    }

    private fun doAction(action: Actions) {
        Logger.warn("Doing action {}", action)
        with(hasskClient) {
            when (action) {
                Actions.TOP -> scene("top_button") turn on
                Actions.MORNING -> scene("early_morning") turn on
                Actions.OFFICE -> group("office_group") turn checkAndToggleState("paper")
                Actions.BEDROOM -> group("bedroom_group") turn checkAndToggleState("shelf_lamp")
                Actions.KITCHEN -> light("kitchen_lights") turn checkAndToggleState("kitchen_lights")
                Actions.TV -> scene("tv") turn on
                Actions.MOVIE -> scene("movie") turn on
                Actions.BEDTIME -> scene("bed_time") turn on
                Actions.LATE_NIGHT -> scene("late_night") turn on
                Actions.NOT_ALL -> group("not_bedroom_group") turn off
            }
        }
    }

    private fun HAssKClient.checkAndToggleState(name: String) = if (light(name).state().state == "off") on else off
}
