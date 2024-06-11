package crackers.kobots.buttonboard.buttons

import crackers.kobots.app.AppCommon
import crackers.kobots.buttonboard.RosetteStatus
import crackers.kobots.buttonboard.TheActions
import crackers.kobots.buttonboard.buttons.HomeAssistantMenus.IMAGE_BED
import crackers.kobots.graphics.loadImage
import crackers.kobots.parts.ORANGISH
import crackers.kobots.parts.app.io.NeoKeyMenu
import crackers.kobots.parts.scheduleWithFixedDelay
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.concurrent.Future
import kotlin.time.Duration.Companion.milliseconds

/**
 * TODO fill this in
 */
object KobotsMenus {
    enum class RobotImages(val image: BufferedImage) {
        STOP(loadImage("/robot/dangerous.png")),
        FLASHLIGHT(loadImage("/robot/flashlight_on.png")),
        HI(loadImage("/robot/hail.png")),
        STOP_IT(loadImage("/robot/halt.png")),
        HOME(loadImage("/robot/home.png")),
        RETURN(loadImage("/robot/redo.png")),
        DROPS(loadImage("/robot/symptoms.png")),
        CLEAR(loadImage("/robot/restart_alt.png")),
    }

    enum class FrontBenchActions {
        STANDARD_ROBOT,
        SHOW_OFF,
        MOPIDI,
    }

    private val homeItem =
        NeoKeyMenu.MenuItem("Home", buttonColor = Color.GREEN, icon = RobotImages.HOME.image) {
            TheActions.GripperActions.HOME()
        }
    private lateinit var blinkyFuture: Future<*>
    private var blinkyState = false

    fun startBlinky() {
        blinkyFuture =
            AppCommon.executor.scheduleWithFixedDelay(500.milliseconds, 500.milliseconds) {
                blinkyState = !blinkyState
                if (blinkyState) {
                    FrontBenchPicker.keyBoard[1] = Color.RED
                } else {
                    FrontBenchPicker.keyBoard[1] = Color.GREEN
                }
            }
    }

    val menuSelections =
        mapOf(
            FrontBenchActions.STANDARD_ROBOT to
                NeoKeyMenu(
                    FrontBenchPicker.keyHandler,
                    FrontBenchPicker.display,
                    listOf(
                        NeoKeyMenu.MenuItem("Drops", icon = RobotImages.DROPS.image, buttonColor = Color.DARK_GRAY) {
                            TheActions.GripperActions.PICKUP()
                        },
                        NeoKeyMenu.MenuItem(
                            "Rtn",
                            icon = RobotImages.RETURN.image,
                            buttonColor = HomeAssistantMenus.DARK_CYAN,
                        ) {
                            TheActions.GripperActions.RETURN()
                            if (::blinkyFuture.isInitialized) blinkyFuture.cancel(true)
                        },
                        homeItem,
                        NeoKeyMenu.MenuItem(
                            "Clear",
                            icon = RobotImages.CLEAR.image,
                            buttonColor = Color.BLUE,
                            action = RosetteStatus::reset,
                        ),
                    ),
                ),
            FrontBenchActions.SHOW_OFF to
                NeoKeyMenu(
                    FrontBenchPicker.keyHandler,
                    FrontBenchPicker.display,
                    listOf(
                        homeItem,
                        NeoKeyMenu.MenuItem(
                            "Excuse Me",
                            "Sry",
                            HomeAssistantMenus.CANCEL_ICON,
                            HomeAssistantMenus.DARK_CYAN,
                        ) { TheActions.GripperActions.EXCUSE_ME() },
                        NeoKeyMenu.MenuItem("Sleep", icon = IMAGE_BED, buttonColor = Color.BLUE.darker()) {
                            TheActions.GripperActions.SLEEP()
                        },
                        NeoKeyMenu.MenuItem(
                            "Hi",
                            icon = RobotImages.HI.image,
                            buttonColor = HomeAssistantMenus.DARK_CYAN,
                        ) {
                            TheActions.GripperActions.SAY_HI()
                        },
                        NeoKeyMenu.MenuItem("Stop", icon = RobotImages.STOP_IT.image, buttonColor = ORANGISH) {
                            TheActions.GripperActions.STOP()
                        },
                        NeoKeyMenu.MenuItem("Flash", icon = RobotImages.FLASHLIGHT.image, buttonColor = Color.YELLOW) {
                            TheActions.GripperActions.FLASHLIGHT()
                        },
                    ),
                ),
//            FrontBenchActions.MOPIDI to
//                audioPlayMenu(
//                    FrontBenchPicker.keyHandler,
//                    FrontBenchPicker.display,
//                ),
        )
}
