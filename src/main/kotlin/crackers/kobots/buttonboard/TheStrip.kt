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
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Run pretty stuff on the Neopixel strip.
 */
object TheStrip {
    private lateinit var seeSaw: AdafruitSeeSaw
    private lateinit var strip: NeoPixel

    // 0 and 360 are the same, so back off (and the 30 makes this even easier)
    private val rainbowColors = colorIntervalFromHSB(0f, 348f, 30)
    private var lastRainbowColorIndex: Int = 0

    private enum class Mode {
        MORNING, DAYTIME, EVENING, NIGHT, PARTY
    }

    private var lastMode: Mode? = null

    private lateinit var future: Future<*>
    private val executor = Executors.newSingleThreadExecutor()

    private val runnable = Runnable {
        while (runFlag.get()) {
            try {
                // check the time of day vs mode
                val now = LocalTime.now()
                when {
                    now.hour < 7 || now.hour >= 23 -> {
                        if (lastMode != Mode.NIGHT) {
                            strip.fill(Color.BLACK)
                            lastMode = Mode.NIGHT
                        }
                    }

                    now.hour < 8 -> {
                        if (lastMode != Mode.MORNING) {
                            strip.brightness = 0.03f
                            strip.fill(GOLDENROD)
                            lastMode = Mode.MORNING
                        }
                    }

                    //                now.hour < 12 -> {
                    //                    if (lastMode != Mode.DAYTIME) {
                    //                        strip.brightness = 0.05f
                    //                        strip.fill(PURPLE)
                    //                        lastMode = Mode.DAYTIME
                    //                    }
                    //                }

                    now.hour >= 21 -> {
                        if (lastMode != Mode.EVENING) {
                            strip.brightness = 0.03f
                            strip.fill(Color.RED)
                            lastMode = Mode.EVENING
                        }
                    }

                    else -> {
                        if (lastMode != Mode.PARTY) {
                            if (lastMode != Mode.MORNING) {
                                strip.brightness = 0.2f
                                lastMode = Mode.PARTY
                            }
                        }
                        showRainbow()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            KobotSleep.seconds(5)
        }

        strip.fill(Color.BLACK)
        seeSaw.softwareReset()
    }

    fun start(): Boolean {
        if (isRemote) return true

        val logger = LoggerFactory.getLogger(this::class.java)
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

    fun stop() {
        if (::future.isInitialized) {
            future.get()
            executor.shutdownNow()
            seeSaw.close()
        }
    }

    private fun showRainbow() {
        for (count in 0..29) {
            runFlag.get() || return
            strip[count] = rainbowColors[lastRainbowColorIndex++]
            if (lastRainbowColorIndex >= 30) lastRainbowColorIndex = 0
            KobotSleep.millis(50)
        }
        lastRainbowColorIndex++
        if (lastRainbowColorIndex >= 30) lastRainbowColorIndex = 0
    }
}
