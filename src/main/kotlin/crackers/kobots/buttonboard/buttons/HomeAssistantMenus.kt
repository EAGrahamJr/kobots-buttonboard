package crackers.kobots.buttonboard.buttons

import crackers.kobots.app.AppCommon
import crackers.kobots.buttonboard.Mode
import crackers.kobots.buttonboard.TheActions.HassActions
import crackers.kobots.buttonboard.TheActions.MopdiyActions
import crackers.kobots.buttonboard.currentMode
import crackers.kobots.parts.GOLDENROD
import crackers.kobots.parts.ORANGISH
import crackers.kobots.parts.PURPLE
import crackers.kobots.parts.app.io.NeoKeyMenu.MenuItem
import crackers.kobots.parts.loadImage
import java.awt.Color

/**
 * TODO fill this in
 */
object HomeAssistantMenus {
    val CANCEL_ICON = loadImage("/cancel.png")
    val DARK_CYAN = Color.CYAN.darker()
    val OLIVE_GREEN = Color(.39f, .38f, .23f) // blech
    val LIGHT_GREEN = Color(125, 255, 0)

    val IMAGE_BED = loadImage("/bed.png")
    val IMAGE_EXIT = loadImage("/exit.png")
    val IMAGE_BULB = loadImage("/lightbulb.png")
    val IMAGE_HOTEL = loadImage("/hotel.png")
    val IMAGE_MOON = loadImage("/moon.png")
    val IMAGE_MOVIE = loadImage("/movie.png")
    val IMAGE_KITCHEN = loadImage("/restaurant.png")
    val IMAGE_SUN = loadImage("/sun.png")
    val IMAGE_TV = loadImage("/tv.png")
    val IMAGE_FAN = loadImage("/fan.png")
    val IMAGE_SKULL = loadImage("/skull.png")
    val IMAGE_LIGHTGROUP = loadImage("/light_group.png")
    val IMAGE_EAR = loadImage("/hearing.png")
    val IMAGE_RELAX = loadImage("/relax.png")

    val nightOffFunction =
        MenuItem("Off", icon = IMAGE_EXIT, buttonColor = ORANGISH) {
            HassActions.NOT_ALL()
            MopdiyActions.STOP()
        }

    val fanControl =
        MenuItem("Fan", icon = IMAGE_FAN, buttonColor = Color.BLUE) {
            HassActions.OFFICE_FAN()
            with(AppCommon.hasskClient) {
                (if (switch("small_fan").state().state == "off") MopdiyActions.PLAY else MopdiyActions.STOP)()
            }
        }

    val morningScene =
        MenuItem("Morn", icon = IMAGE_SUN, buttonColor = GOLDENROD) {
            HassActions.MORNING()
            MopdiyActions.PLAY()
        }

    val daytimeScene = MenuItem("Day", icon = IMAGE_BULB, buttonColor = Color.GREEN) { HassActions.DAYTIME() }

    val kitchenLights =
        MenuItem("Kit", icon = IMAGE_KITCHEN, buttonColor = Color.CYAN) {
            HassActions.KITCHEN()
        }

    val tvViewing =
        MenuItem("TV", icon = IMAGE_TV, buttonColor = PURPLE) {
            HassActions.TV()
            MopdiyActions.STOP()
        }

    val movieViewing =
        MenuItem("Movie", icon = IMAGE_MOVIE, buttonColor = Color.RED.darker()) {
            HassActions.MOVIE()
            MopdiyActions.STOP()
        }

    val manualMode =
        MenuItem("Manual", icon = KobotsMenus.RobotImages.STOP_IT.image, buttonColor = DARK_CYAN) {
            currentMode = Mode.MANUAL
        }

    val bedtimeScene = MenuItem("BTime", icon = IMAGE_HOTEL, buttonColor = Color.PINK) { HassActions.BEDTIME() }

    val lateNightScene = MenuItem("Late", icon = IMAGE_MOON, buttonColor = DARK_CYAN) { HassActions.LATE_NIGHT() }

    val tfeScene =
        MenuItem("TFE", icon = IMAGE_SKULL, buttonColor = Color.RED.darker()) { HassActions.TFE() }

    val bedroomToggle = MenuItem("BRm", icon = IMAGE_BED, buttonColor = Color.PINK) { HassActions.BEDROOM() }
    val allOn = MenuItem("All", icon = IMAGE_LIGHTGROUP, buttonColor = LIGHT_GREEN) {}

    val whiteNoiseToggle = MenuItem("EAR", icon = IMAGE_EAR, buttonColor = ORANGISH) {}
    val postTVScene = MenuItem("Post", icon = IMAGE_RELAX, buttonColor = Color.YELLOW) { HassActions.POST_TV() }
}
