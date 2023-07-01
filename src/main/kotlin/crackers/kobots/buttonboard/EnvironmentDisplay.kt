package crackers.kobots.buttonboard

import com.apptasticsoftware.rssreader.RssReader
import crackers.kobots.buttonboard.TheActions.hasskClient
import crackers.kobots.devices.display.SSD1327
import crackers.kobots.utilities.center
import crackers.kobots.utilities.loadImage
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import kotlin.properties.Delegates

/**
 * Weather and agenda display.
 */
object EnvironmentDisplay : Runnable {
    val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var future: Future<*>
    private val executor = Executors.newSingleThreadScheduledExecutor()

    private lateinit var screen: SSD1327
    private val screenGraphics: Graphics2D
    private val image: BufferedImage

    private val tempFont = Font(Font.SANS_SERIF, Font.PLAIN, 32)
    private val tempFontMetrics: FontMetrics
    private val agendaFont = Font(Font.SANS_SERIF, Font.PLAIN, 9)
    private val agentaFontMetrics: FontMetrics
    private val agendaLineHeight: Int

    private const val MW = 128
    private const val MH = 128
    private const val TEMP_HEIGHT = 40
    private var bottomStartsAt: Int by Delegates.notNull()

    init {
        image = BufferedImage(MW, MH, BufferedImage.TYPE_BYTE_GRAY).also { img: BufferedImage ->
            screenGraphics = (img.graphics as Graphics2D).also {
                tempFontMetrics = it.getFontMetrics(tempFont)
                agentaFontMetrics = it.getFontMetrics(agendaFont)
                agendaLineHeight = agentaFontMetrics.height + 1
            }
        }
    }

    private val images by lazy {
        mapOf(
            "clear-night" to loadImage("/weather/clear-night.png"),
            "cloudy" to loadImage("/weather/cloudy.png"),
            "fog" to loadImage("/weather/fog.png"),
            "mixed" to loadImage("/weather/mixed.png"),
            "partlycloudy" to loadImage("/weather/partly-cloudy.png"),
            "rain" to loadImage("/weather/rain.png"),
            "snow" to loadImage("/weather/snow.png"),
            "sunny" to loadImage("/weather/sunny.png"),
            "windy" to loadImage("/weather/windy.png"),
            "default" to loadImage("/screaming.png")
        )
    }

    fun start() {
        screen = SSD1327(SSD1327.ADAFRUIT_STEMMA).apply {
            displayOn = false
            clear()
        }
        future = executor.scheduleAtFixedRate(this, 1, 600, TimeUnit.SECONDS)
    }

    fun stop() {
        future.cancel(false)

        screen.displayOn = false
        screen.close()
        executor.shutdownNow()
    }

    override fun run() {
        // leave it off at night
        val now = LocalDateTime.now()
        if (now.let { it.hour >= 23 || it.hour <= 6 }) {
            screen.displayOn = false
            return
        }
        try {
            if (!screen.displayOn) {
                screen.displayOn = true
                // assuming this happens once a day, update the date
                screenGraphics.showDate(now)
            }
            screenGraphics.showOutside()
//            screenGraphics.showAgenda(now)
            screenGraphics.showNews()

            with(screen) {
                display(image)
                show()
            }
        } catch (t: Throwable) {
            LoggerFactory.getLogger(this::class.java).error("Unable to display", t)
        }
    }

    /**
     * Shows the date in the top line for the agenda block.
     */
    private fun Graphics2D.showDate(now: LocalDateTime) {
        color = Color.WHITE
        font = Font(Font.SANS_SERIF, Font.BOLD, 12)
        val fm = getFontMetrics(font)
        val date = "${now.dayOfWeek.name.substring(0, 3)}  ${now.month} ${now.dayOfMonth}"
        val x = fm.center(date, MW)
        drawString(date, x, TEMP_HEIGHT + fm.height)
        bottomStartsAt = TEMP_HEIGHT + fm.height + 2
    }

    private var lastState: String? = null
    private var lastTemp: Int? = null

    /**
     * Show the outside temperature and weather icon when it changes.
     */
    private fun Graphics2D.showOutside() {
        val outsideTemp = hasskClient.getState("weather.home")
        val state = outsideTemp.state
        val temp = JSONObject(outsideTemp.attributes).getInt("temperature")

        if (state == lastState && temp == lastTemp) return
        lastState = state
        lastTemp = temp

        // clear the top area
        color = Color.BLACK
        fillRect(0, 0, MW, TEMP_HEIGHT)

        val icon = images[state] ?: images["default"].also {
            logger.warn("Unknown weather state: $state")
        }
        scaleImageAt(icon!!, 0, 0, TEMP_HEIGHT)

        font = tempFont
        color = when {
            temp > 79 -> Color.YELLOW
            temp > 50 -> Color.GREEN
            temp > 40 -> Color.CYAN
            else -> Color.BLUE
        }
        drawString("$temp\u2109", 50, tempFontMetrics.ascent)
    }

    private fun Graphics2D.scaleImageAt(image: BufferedImage, x: Int, y: Int, width: Int, height: Int = width) {
        drawImage(image, x, y, width, height, null)
    }

    private const val RSS_FEED = "http://feeds.washingtonpost.com/rss/national?itid=lk_inline_manual_32"
    private val reader = RssReader()

    private fun Graphics2D.showNews() {
        val feed = reader.read(RSS_FEED).collect(Collectors.toList()).map { "* ${it.title.get()}" }
        showBottom(feed)
    }

    private fun Graphics2D.showBottom(lines: List<String>) {
        color = Color.BLACK
        fillRect(0, bottomStartsAt, MW, MH)

        color = Color.WHITE
        font = agendaFont
        lines.forEachIndexed { index, line ->
            drawString(line, 0, bottomStartsAt + (index + 1) * agendaLineHeight)
        }
    }
}
