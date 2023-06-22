package crackers.kobots.buttonboard

import com.diozero.api.I2CDevice
import com.diozero.devices.oled.SSD1306
import com.diozero.devices.oled.SsdOledCommunicationChannel
import crackers.kobots.utilities.loadImage
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicReference

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

    private val image = BufferedImage(128, HT, BufferedImage.TYPE_BYTE_GRAY).also { img ->
        screenGraphics = (img.graphics as Graphics2D)
    }
    private val lastMode = AtomicReference<Mode>()

    internal enum class Images(val image: BufferedImage) {
        BED(loadImage("/bed.png")),
        EXIT(loadImage("/exit.png")),
        LIGHTBULB(loadImage("/lightbulb.png")),
        MOON(loadImage("/moon.png")),
        MOVIE(loadImage("/movie.png")),
        PRINTER(loadImage("/printer.png")),
        RESTAURANT(loadImage("/restaurant.png")),
        SUN(loadImage("/sun.png")),
        TV(loadImage("/tv.png"))
    }

    /**
     * Show the icons on the screen based on the mode.
     */
    internal fun showIcons(mode: Mode) {
        if (mode != lastMode.getAndSet(mode)) {
            with(screenGraphics) {
                // clear the image
                color = Color.BLACK
                fillRect(0, 0, 128, HT)
                // draw and scale the icons
                mode.images.forEachIndexed { index, icon ->
                    drawImage(icon.image, index * HT, 0, HT, HT, null)
                }
            }
            screen.display(image)
        }
    }

    fun close() {
        screen.clear()
        screen.setDisplayOn(false)
        screen.close()
    }
}
