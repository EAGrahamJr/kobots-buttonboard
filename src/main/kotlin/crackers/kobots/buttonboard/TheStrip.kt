package crackers.kobots.buttonboard

import com.diozero.api.I2CDevice
import com.diozero.util.SleepUtil
import crackers.kobots.devices.lighting.NeoPixel
import crackers.kobots.devices.microcontroller.AdafruitSeeSaw
import crackers.kobots.utilities.GOLDENROD
import crackers.kobots.utilities.KobotSleep
import crackers.kobots.utilities.colorIntervalFromHSB
import org.slf4j.LoggerFactory
import java.awt.Color
import java.util.concurrent.Future

/**
 * Run pretty stuff on the Neopixel strip.
 */
object TheStrip {
    private val logger = LoggerFactory.getLogger("TheStrip")
    private lateinit var seeSaw: AdafruitSeeSaw
    private lateinit var strip: NeoPixel

    // 0 and 360 are the same, so back off (and the 30 makes this even easier)
    private val rainbowColors = colorIntervalFromHSB(0f, 348f, 30)
    private var lastRainbowColorIndex: Int = 0

    private var lastMode: Mode? = null

    private lateinit var future: Future<*>

    private val stripOffset = 0
    private val stripLast = stripOffset + 29

    fun start(): Boolean {
        if (isRemote) return true

        for (i in 0 until 100) try {
            seeSaw = AdafruitSeeSaw(I2CDevice(1, 0x60))
            strip = NeoPixel(seeSaw, 30, 15)
            logger.warn("Took $i tries to initialize")

            strip.brightness = 0.1f
            strip.autoWrite = true

            future = executor.submit(runnable)
            return true
        } catch (_: Throwable) {
            SleepUtil.busySleep(50)
        }
        logger.error("Failed to initialize")
        return false
    }

    private val runnable = Runnable {
        while (runFlag) {
            if (currentMode != lastMode) {
                try {
                    lastMode = currentMode
                    // check the time of day vs mode

                    when (currentMode) {
                        Mode.NIGHT -> strip[stripOffset, stripLast] = Color.BLACK
                        Mode.MORNING -> {
                            strip.brightness = 0.03f
                            strip[stripOffset, stripLast] = GOLDENROD
                        }

                        Mode.DAYTIME -> {
                            strip.brightness = 0.2f
                        }

                        Mode.EVENING -> {
                            strip.brightness = 0.03f
                            strip[stripOffset, stripLast] = Color.RED
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Cannot display strip", e)
                }
            }
            if (currentMode == Mode.DAYTIME) showRainbow()
            KobotSleep.seconds(10)
        }
        // turn off the strip
        strip.fill(Color.BLACK)
    }

    fun stop() {
        if (::future.isInitialized) {
            future.cancel(true)
            seeSaw.close()
        }
    }

    private fun showRainbow() {
        for (count in stripOffset..stripLast) {
            runFlag || return
            strip[count] = rainbowColors[lastRainbowColorIndex++]
            if (lastRainbowColorIndex >= 30) lastRainbowColorIndex = 0
            KobotSleep.millis(50)
        }
        lastRainbowColorIndex++
        if (lastRainbowColorIndex >= 30) lastRainbowColorIndex = 0
    }
}
