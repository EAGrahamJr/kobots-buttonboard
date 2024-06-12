package crackers.kobots.buttonboard.buttons

import crackers.kobots.app.AppCommon
import crackers.kobots.buttonboard.GraphicsStuff.DARK_CYAN
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_BED
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_BULB
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_EAR
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_EXIT
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_FAN
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_HOTEL
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_KITCHEN
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_LIGHTGROUP
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_MOON
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_MOVIE
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_PRINTER
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_RELAX
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_SKULL
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_SUN
import crackers.kobots.buttonboard.GraphicsStuff.IMAGE_TV
import crackers.kobots.buttonboard.GraphicsStuff.LIGHT_GREEN
import crackers.kobots.buttonboard.TheActions.HassActions
import crackers.kobots.buttonboard.TheActions.MusicPlayActions
import crackers.kobots.parts.GOLDENROD
import crackers.kobots.parts.ORANGISH
import crackers.kobots.parts.PURPLE
import crackers.kobots.parts.app.io.NeoKeyMenu.MenuItem
import java.awt.Color

/**
 * Ibid
 */
object HomeAssistantMenus {
    val nightOffFunction =
        MenuItem("Off", icon = IMAGE_EXIT, buttonColor = GOLDENROD) {
            HassActions.NOT_ALL()
            MusicPlayActions.STOP()
        }

    val fanControl =
        MenuItem("Fan", icon = IMAGE_FAN, buttonColor = Color.BLUE) {
            HassActions.OFFICE_FAN()
            with(AppCommon.hasskClient) {
                (if (switch("small_fan").state().state == "off") MusicPlayActions.PLAY else MusicPlayActions.STOP)()
            }
        }

    val morningScene =
        MenuItem("Morn", icon = IMAGE_SUN, buttonColor = GOLDENROD) {
            HassActions.MORNING()
            MusicPlayActions.PLAY()
        }

    val daytimeScene = MenuItem("Day", icon = IMAGE_BULB, buttonColor = Color.GREEN) { HassActions.DAYTIME() }
    val kitchenLights = MenuItem("Kit", icon = IMAGE_KITCHEN, buttonColor = Color.CYAN) { HassActions.KITCHEN() }

    val tvViewing =
        MenuItem("TV", icon = IMAGE_TV, buttonColor = PURPLE) {
            HassActions.TV()
            MusicPlayActions.STOP()
        }

    val movieViewing =
        MenuItem("Movie", icon = IMAGE_MOVIE, buttonColor = Color.RED.darker()) {
            HassActions.MOVIE()
            MusicPlayActions.STOP()
        }

    val printerToggle = MenuItem("Pntr", icon = IMAGE_PRINTER, buttonColor = GOLDENROD) { HassActions.THING_PRINTER() }
    val bedtimeScene = MenuItem("BTime", icon = IMAGE_HOTEL, buttonColor = Color.PINK) { HassActions.BEDTIME() }
    val lateNightScene = MenuItem("Late", icon = IMAGE_MOON, buttonColor = DARK_CYAN) { HassActions.LATE_NIGHT() }
    val tfeScene = MenuItem("TFE", icon = IMAGE_SKULL, buttonColor = Color.RED.darker()) { HassActions.TFE() }
    val bedroomToggle = MenuItem("BRm", icon = IMAGE_BED, buttonColor = Color.PINK) { HassActions.BEDROOM() }
    val allOn = MenuItem("All", icon = IMAGE_LIGHTGROUP, buttonColor = LIGHT_GREEN) { HassActions.ALL_LIGHTS() }
    val whiteNoiseToggle = MenuItem("EAR", icon = IMAGE_EAR, buttonColor = ORANGISH) { MusicPlayActions.TOGGLE() }
    val postTVScene = MenuItem("Post", icon = IMAGE_RELAX, buttonColor = Color.YELLOW) { HassActions.POST_TV() }
}
