package crackers.kobots.buttonboard

import com.diozero.api.I2CDevice
import com.diozero.devices.oled.SSD1306
import com.diozero.devices.oled.SsdOledCommunicationChannel
import com.diozero.sbc.LocalSystemInfo
import crackers.kobots.buttonboard.TheActions.hasskClient
import crackers.kobots.utilities.loadImage
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * TODO fill this in
 */
object EnvironmentDisplay : Runnable {
    private lateinit var screen: SSD1306
    private val screenGraphics: Graphics2D
    private val image: BufferedImage

    private lateinit var future: Future<*>
    private val executor = Executors.newSingleThreadScheduledExecutor()

    private val tempFont = Font(Font.SANS_SERIF, Font.PLAIN, 24)
    private val tempFontMetrics: FontMetrics
    private val smallFont = Font(Font.SANS_SERIF, Font.PLAIN, 13)
    private val smallFontMetrics: FontMetrics
    private val thermImage by lazy { loadImage("/thermometer2.png") }

    private const val MW = 128
    private const val MH = 32
    private const val TLOC = 75

    private val eekImage by lazy { loadImage("/screaming.png") }

    init {
        image = BufferedImage(MW, MH, BufferedImage.TYPE_BYTE_GRAY).also { img: BufferedImage ->
            screenGraphics = (img.graphics as Graphics2D).also {
                tempFontMetrics = it.getFontMetrics(tempFont)
                smallFontMetrics = it.getFontMetrics(smallFont)
            }
        }
    }

    private val centerLine = smallFontMetrics.ascent + (MH - smallFontMetrics.height) / 2

    fun start() {
        val channel = SsdOledCommunicationChannel.I2cCommunicationChannel(I2CDevice(1, SSD1306.DEFAULT_I2C_ADDRESS))
        screen = SSD1306(channel, SSD1306.Height.SHORT).apply {
            clear()
            setDisplayOn(true)
            setContrast(0x20.toByte())
        }

        future = executor.scheduleAtFixedRate(this, 1, 10, TimeUnit.SECONDS)
    }

    fun stop() {
        screen.setDisplayOn(false)
        future.cancel(false)
        executor.shutdownNow()
    }

    private enum class WHICH {
        Office, LivingRoom, Bedroom, CPU, Outside;

        fun next(): WHICH {
            var n = ordinal + 1
            if (n >= WHICH.values().size) n = 0
            return WHICH.values()[n]
        }
    }

    private var nextToShow = WHICH.Outside

    override fun run() {
        try {
//            if (mainScreen.on) {
//                screen.setDisplayOn(true)
//            } else {
//                with(hasskClient) {
//                    if (light("cabinets").state().state == "off") {
//                        screen.setDisplayOn(false)
//                        return
//                    }
//                }
//            }

            with(screenGraphics) {
                color = Color.BLACK
                fillRect(0, 0, MW, MH)
                val which = nextToShow.next()
                nextToShow = which
                when (which) {
                    WHICH.Outside -> showOutside()
                    WHICH.CPU -> showCPU()
                    else -> showInside(which)
                }
            }
            screen.display(image)
        } catch (t: Throwable) {
            LoggerFactory.getLogger(this::class.java).error("Unable to display $nextToShow", t)
        }
    }

    private fun Graphics2D.showOutside() {
        color = Color.WHITE
        font = smallFont

        val outsideTemp = hasskClient.getState("weather.home")
        val temp = JSONObject(outsideTemp.attributes).getInt("temperature")

        drawString(outsideTemp.state, 0, centerLine)
        scaleImageAt(thermImage, 45, 0, MH)
        font = tempFont
        color = when {
            temp > 79 -> Color.YELLOW
            temp > 50 -> Color.GREEN
            temp > 40 -> Color.CYAN
            else -> Color.BLUE
        }
        drawString("$temp\u2109", TLOC, tempFontMetrics.ascent)
    }

    private fun Graphics2D.showCPU() {
        val tsize = MH / 2
        scaleImageAt(thermImage, 0, tsize / 2, tsize)
        color = Color.GREEN
        font = smallFont.deriveFont(16)
        drawString("CPU", tsize, centerLine)

        font = tempFont
        color = Color.WHITE
        drawString("${LocalSystemInfo.getInstance().cpuTemperature.toInt()}\u2103", TLOC, tempFontMetrics.ascent)
    }

    private fun Graphics2D.showInside(which: WHICH) {
        val tsize = MH / 2
        scaleImageAt(thermImage, 0, tsize / 2, tsize)
        color = Color.GREEN
        font = smallFont
        val temp = when (which) {
            WHICH.Office -> {
                drawString("Office", tsize, centerLine)
                getTemperature("sensor.trisensor_air_temperature")
            }

            WHICH.LivingRoom -> {
                drawString("Living", tsize, smallFontMetrics.ascent)
                drawString("Room", tsize, MH)
                getTemperature("sensor.cube_air_temperature")
            }

            WHICH.Bedroom -> {
                drawString("Bedroom", tsize, centerLine)
                getTemperature("sensor.bedroom_temperature")
            }

            else -> throw IllegalStateException()
        }

        font = tempFont
        color = Color.WHITE
        if (temp == 0) {
            scaleImageAt(eekImage, TLOC, 0, MH)
        }
        drawString("$temp\u2109", TLOC, tempFontMetrics.ascent)
    }

    private fun getTemperature(sensor: String) =
        hasskClient.getState(sensor).state.let {
            if (it == "unavailable") "0" else it
        }.toFloat().toInt()

    private fun Graphics2D.scaleImageAt(image: BufferedImage, x: Int, y: Int, width: Int, height: Int = width) {
        drawImage(image, x, y, width, height, null)
    }
}
