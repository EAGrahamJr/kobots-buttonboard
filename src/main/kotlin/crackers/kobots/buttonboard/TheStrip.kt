package crackers.kobots.buttonboard

import com.diozero.api.I2CDevice
import com.diozero.util.SleepUtil
import crackers.kobots.devices.expander.AdafruitSeeSaw
import crackers.kobots.devices.lighting.NeoPixel
import crackers.kobots.utilities.GOLDENROD
import crackers.kobots.utilities.colorIntervalFromHSB
import org.slf4j.LoggerFactory
import java.awt.Color
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Run pretty stuff on the Neopixel strip.
 */
object TheStrip {
    private lateinit var strip: NeoPixel

    // 0 and 360 are the same, so back off (and the 30 makes this even easier)
    private val rainbowColors = colorIntervalFromHSB(0f, 348f, 30)
    private var lastRainbowColorIndex: Int = 0

    private enum class Mode {
        MORNING, DAYTIME, EVENING, NIGHT, PARTY
    }

    private var lastMode: Mode? = null

    private lateinit var future: Future<*>
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var running = true
    private val runnable = Runnable {
        val pause = Duration.ofSeconds(3).toNanos()
        while (running) {
            try {
                // check the time of day vs mode
                val now = LocalTime.now()
                when {
                    now.hour < 7 || now.hour >= 23 -> {
                        if (lastMode != Mode.NIGHT) {
                            strip.autoWrite = true
                            strip.fill(Color.BLACK)
                            lastMode = Mode.NIGHT
                        }
                    }

                    now.hour < 8 -> {
                        if (lastMode != Mode.MORNING) {
                            strip.autoWrite = true
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
                            strip.autoWrite = true
                            strip.brightness = 0.03f
                            strip.fill(Color.RED)
                            lastMode = Mode.EVENING
                        }
                    }

                    else -> {
                        if (lastMode != Mode.PARTY) {
                            strip.autoWrite = false
                            if (lastMode != Mode.MORNING) {
                                strip.brightness = 0.5f
                                lastMode = Mode.PARTY
                            }
                        }
                        showRainbow()
                        strip.show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            SleepUtil.busySleep(pause)
        }
    }

    fun start() {
        for (i in 0 until 100) try {
            strip = NeoPixel(AdafruitSeeSaw(I2CDevice(1, 0x60)), 30, 15)
            LoggerFactory.getLogger(this::class.java).warn("Took $i tries to initialize")
            break
        } catch (_: Throwable) {
            SleepUtil.busySleep(50)
        }

        strip.brightness = 0.1f

        future = executor.submit(runnable)
    }

    fun stop() {
        running = false
        future.get()
        executor.shutdownNow()
    }

    private fun showRainbow() {
        for (count in 0..29) {
            strip[count] = rainbowColors[lastRainbowColorIndex++]
            if (lastRainbowColorIndex >= 30) lastRainbowColorIndex = 0
        }
        lastRainbowColorIndex++
        if (lastRainbowColorIndex >= 30) lastRainbowColorIndex = 0
    }
}
