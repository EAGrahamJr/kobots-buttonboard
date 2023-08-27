package crackers.kobots.buttonboard

import com.diozero.api.I2CDevice
import com.diozero.devices.oled.SSD1306
import com.diozero.devices.oled.SsdOledCommunicationChannel
import crackers.kobots.utilities.center
import crackers.kobots.utilities.loadImage
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage

/**
 * TODO fill this in
 */
object TheScreen {
    private val screen = let {
        val channel = SsdOledCommunicationChannel.I2cCommunicationChannel(I2CDevice(1, SSD1306.DEFAULT_I2C_ADDRESS))
        SSD1306(channel, SSD1306.Height.SHORT).apply {
            clear()
            setDisplayOn(true)
            setContrast(0x20.toByte())
        }
    }
    private val screenGraphics: Graphics2D
    private val HT = 32

    private val textHeight: Int
    private val textBaseline: Int
    private val imageSize: Int
    private val image = BufferedImage(128, HT, BufferedImage.TYPE_BYTE_GRAY).also { img ->
        screenGraphics = (img.graphics as Graphics2D).apply {
            font = Font(Font.SANS_SERIF, Font.PLAIN, 9)
            textHeight = fontMetrics.height
            textBaseline = HT - fontMetrics.descent
            imageSize = HT - textHeight
        }
    }

    internal enum class Images(val image: BufferedImage) {
        BED(loadImage("/bed.png")),
        EXIT(loadImage("/exit.png")),
        LIGHTBULB(loadImage("/lightbulb.png")),
        MOON(loadImage("/moon.png")),
        MOVIE(loadImage("/movie.png")),
        PRINTER(loadImage("/printer.png")),
        RESTAURANT(loadImage("/restaurant.png")),
        SUN(loadImage("/sun.png")),
        TV(loadImage("/tv.png")),
        FAN(loadImage("/fan.png"))
    }

    /**
     * Show the icons on the screen based on the mode.
     */
    internal fun showIcons(mode: Mode) {
        with(screenGraphics) {
            // clear the image
            color = Color.BLACK
            fillRect(0, 0, 128, HT)
            // draw and scale the icons
            color = Color.WHITE
            mode.images.forEachIndexed { index, icon ->
                val offset = index * HT
                val imageX = offset + textHeight / 2
                drawImage(icon.image, imageX, 0, imageSize, imageSize, null)
                val textX = offset + fontMetrics.center(mode.text[index], HT)
                drawString(mode.text[index], textX, textBaseline)
            }
        }
        screen.display(image)
        screen.setContrast((if (mode == Mode.NIGHT) 0x05 else 0x20).toByte())
    }

    internal fun showText(text: String) {
//        with(screenGraphics) {
//            color = Color.BLACK
//            fillRect(0, 0, 128, HT)
//            color = Color.WHITE
//            drawString(text, 0, HT - 4)
//        }
//        screen.display(image)
    }

    fun close() {
        screen.clear()
        screen.setDisplayOn(false)
        screen.close()
    }
}
