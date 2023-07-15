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
        TOP, MORNING, OFFICE, BEDROOM, KITCHEN, TV, MOVIE, BEDTIME, LATE_NIGHT, NOT_ALL, OFFICE_FAN
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
                Actions.OFFICE -> group("office_group") turn toggleOnLight("paper")
                Actions.BEDROOM -> group("bedroom_group") turn toggleOnLight("shelf_lamp")
                Actions.KITCHEN -> light("kitchen_lights") turn toggleOnLight("kitchen_lights")
                Actions.TV -> scene("daytime_tv") turn on
                Actions.MOVIE -> scene("movie_time") turn on
                Actions.BEDTIME -> scene("bed_time") turn on
                Actions.LATE_NIGHT -> scene("late_night") turn on
                Actions.NOT_ALL -> group("not_bedroom_group") turn off
                Actions.OFFICE_FAN -> switch("small_fan") turn toggleOnSwitch("small_fan")
            }
        }
    }

    private fun HAssKClient.toggleOnSwitch(name: String) = if (switch(name).state().state == "off") on else off

    private fun HAssKClient.toggleOnLight(name: String) = if (light(name).state().state == "off") on else off
}
