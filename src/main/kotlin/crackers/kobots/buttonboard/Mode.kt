package crackers.kobots.buttonboard

import crackers.kobots.buttonboard.TheActions.Actions
import crackers.kobots.buttonboard.TheScreen.Images
import crackers.kobots.utilities.GOLDENROD
import crackers.kobots.utilities.PURPLE
import java.awt.Color

/**
 * Shared stuff
 */
internal enum class Mode(
    val actions: List<Actions>,
    val images: List<Images>,
    val colors: List<Color>,
    val brightness: Float = 0.1f
) {
    NIGHT(
        listOf(Actions.TOP, Actions.MORNING, Actions.OFFICE, Actions.BEDROOM),
        listOf(Images.LIGHTBULB, Images.SUN, Images.PRINTER, Images.BED),
        listOf(Color.GREEN, GOLDENROD, Color.ORANGE, Color.PINK),
        .01f
    ),
    MORNING(
        listOf(Actions.MORNING, Actions.TOP, Actions.KITCHEN, Actions.BEDROOM),
        listOf(Images.SUN, Images.LIGHTBULB, Images.RESTAURANT, Images.BED),
        listOf(GOLDENROD, Color.GREEN, Color.CYAN, Color.PINK),
        .05f
    ),
    DAYTIME(
        listOf(Actions.TOP, Actions.TV, Actions.MOVIE, Actions.BEDROOM),
        listOf(Images.LIGHTBULB, Images.TV, Images.MOVIE, Images.BED),
        listOf(Color.GREEN, PURPLE, Color.RED, Color.PINK)
    ),
    EVENING(
        listOf(Actions.BEDTIME, Actions.LATE_NIGHT, Actions.NOT_ALL, Actions.TOP),
        listOf(Images.BED, Images.MOON, Images.EXIT, Images.LIGHTBULB),
        listOf(Color.PINK, Color.RED, Color.BLUE, Color.GREEN),
        .05f
    )
}
