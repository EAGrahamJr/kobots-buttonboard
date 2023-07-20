package crackers.kobots.buttonboard

import com.apptasticsoftware.rssreader.RssReader
import crackers.kobots.buttonboard.TheActions.hasskClient
import crackers.kobots.devices.display.SSD1327
import crackers.kobots.utilities.KobotSleep
import crackers.kobots.utilities.center
import crackers.kobots.utilities.elapsed
import crackers.kobots.utilities.loadImage
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

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
    private val dateFont = Font(Font.SANS_SERIF, Font.BOLD, 14)
    private val dateFontMetrics: FontMetrics
    private val agendaFont = Font(Font.SANS_SERIF, Font.PLAIN, 11)
    private val agendaFontMetrics: FontMetrics
    private val agendaLineHeight: Int

    private const val MAX_W = 128
    private const val MAX_H = 128
    private const val TEMP_HEIGHT = 40

    init {
        image = BufferedImage(MAX_W, MAX_H, BufferedImage.TYPE_BYTE_GRAY).also { img: BufferedImage ->
            screenGraphics = (img.graphics as Graphics2D).also {
                tempFontMetrics = it.getFontMetrics(tempFont)
                agendaFontMetrics = it.getFontMetrics(agendaFont)
                agendaLineHeight = agendaFontMetrics.height + 1
                dateFontMetrics = it.getFontMetrics(dateFont)
            }
        }
    }

    private val dateBottom = TEMP_HEIGHT + dateFontMetrics.height
    private val newsStartsAt = dateBottom + 5

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

    private const val SLEEP_SECONDS = 600L
    private const val HEADLINE_PAUSE = 15L

    fun start() {
        screen = SSD1327(SSD1327.ADAFRUIT_STEMMA).apply {
            displayOn = false
            clear()
        }
        future = executor.scheduleAtFixedRate(this, 1, SLEEP_SECONDS, TimeUnit.SECONDS)
    }

    fun stop() {
        future.cancel(false)

        screen.displayOn = false
        screen.close()
        executor.shutdownNow()
    }

    override fun run() {
        // leave it off at night
        val localNow = LocalDateTime.now()
        if (localNow.hour >= 22 || localNow.hour <= 6) {
            screen.displayOn = false
            return
        }
        try {
            if (!screen.displayOn) {
                screen.displayOn = true
                // assuming this happens once a day, update the date
                screenGraphics.showDate(localNow)
            }
            screenGraphics.showOutside()

            // 5 minutes to show headlines, so run a loop for slightly less than 5 minutes
            val now = Instant.now()
            val stopAt = SLEEP_SECONDS - 10L
            val feed = retrieveNewsHeadlines()
            while (now.elapsed() < Duration.ofSeconds(stopAt)) {
                screenGraphics.showNews(feed)
            }
        } catch (t: Throwable) {
            LoggerFactory.getLogger(this::class.java).error("Unable to display", t)
        }
    }

    /**
     * Shows the date in the top line for the agenda block.
     */
    private fun Graphics2D.showDate(now: LocalDateTime) {
        color = Color.BLACK
        fillRect(0, 0, MAX_W, MAX_H)

        color = Color.WHITE
        font = dateFont
        val date = "${now.dayOfWeek.name.substring(0, 3)}  ${now.month} ${now.dayOfMonth}"
        val x = dateFontMetrics.center(date, MAX_W)
        drawString(date, x, dateBottom)
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
        fillRect(0, 0, MAX_W, TEMP_HEIGHT)

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

    private const val RSS_FEED = "http://feeds.washingtonpost.com/rss/politics?itid=lk_inline_manual_32"
    private val reader = RssReader()

    private fun Graphics2D.showNews(feed: List<String>) {
        // the top headlines are shown for 10 seconds each, wrapping the text as needed to fit the screen
        feed.forEach { headline ->
            val lines = headline.wrap()
            showBottom(lines)
            with(screen) {
                display(image)
                show()
            }
            KobotSleep.seconds(HEADLINE_PAUSE)
        }
    }

    private fun retrieveNewsHeadlines() = reader.read(RSS_FEED).collect(Collectors.toList()).map { it.title.get() }

    private fun Graphics2D.showBottom(lines: List<String>) {
        color = Color.BLACK
        fillRect(0, newsStartsAt, MAX_W, MAX_H)

        color = Color.WHITE
        font = agendaFont
        lines.forEachIndexed { index, line ->
            drawString(line, 0, newsStartsAt + (index + 1) * agendaLineHeight)
        }
    }

    /**
     * Wraps the text to fit the screen by splitting on spaces and hyphens.
     */
    private fun String.wrap(): List<String> {
        val words = split(" ")
        val lines = mutableListOf<String>()
        // assume the first word fits
        var line = words[0]
        for (i in 1 until words.size) {
            val word = words[i]
            val w = agendaFontMetrics.stringWidth(line + " " + word)
            if (w > MAX_W) {
                lines.add(line)
                line = word
            } else {
                line += " " + word
            }
        }
        return lines
    }
}
