package crackers.kobots.buttonboard

import crackers.kobots.app.AppCommon
import crackers.kobots.devices.io.QwiicTwist
import crackers.kobots.parts.GOLDENROD
import java.awt.Color
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Rotary encoder.
 */
object Rooty : AppCommon.Startable {
    private lateinit var encoder: QwiicTwist

    private enum class TwistMode {
        NIGHT,
        PANIC,
        AUDIO,
        LIGHT,
        OFF,
    }

    override fun start() {
        encoder =
            QwiicTwist(i2cMultiplexer.getI2CDevice(4, QwiicTwist.DEFAULT_I2C_ADDRESS)).apply {
                pixel.brightness = .1f
                pixel.fill(Color.YELLOW)
                clearInterrupts() // clear buffers
            }
    }

    private var lastMode = Mode.NONE
    private var twistMode = TwistMode.OFF
    private var lastTwist = TwistMode.OFF

    private val syncLock = ReentrantLock()

    fun clickOrTwist() =
        syncLock.withLock {
            if (lastMode != currentMode) {
                // check if we need to change what we're doing
                lastMode = currentMode
                twistMode =
                    when (currentMode) {
                        Mode.NONE -> TwistMode.OFF
                        Mode.NIGHT -> TwistMode.NIGHT
                        Mode.MORNING -> TwistMode.PANIC
                        Mode.DAYTIME -> if (lastTwist != TwistMode.AUDIO) TwistMode.PANIC else lastTwist
                        Mode.EVENING -> if (lastTwist != TwistMode.AUDIO) TwistMode.PANIC else lastTwist
                    }
            }
            // effect any changes necessary
            if (twistMode != lastTwist) {
                lastTwist = twistMode
                with(encoder.pixel) {
                    when (twistMode) {
                        TwistMode.NIGHT -> {
                            fill(Color.PINK)
                            brightness = .03f
                        }

                        TwistMode.PANIC -> {
                            fill(Color.RED)
                            brightness = .1f
                        }

                        TwistMode.AUDIO -> {
                            fill(GraphicsStuff.LIGHT_GREEN)
                            brightness = .07f
                        }

                        TwistMode.LIGHT -> {
                            fill(GOLDENROD)
                            brightness = .07f
                        }

                        TwistMode.OFF -> off()
                    }
                }
            }

            // and now we check things
            if (encoder.clicked) {
                when (twistMode) {
                    TwistMode.NIGHT -> TheActions.HassActions.ALL_LIGHTS()
                    TwistMode.PANIC -> TheActions.HassActions.ALL_LIGHTS()
                    TwistMode.AUDIO -> TODO()
                    TwistMode.LIGHT -> TODO()
                    TwistMode.OFF -> {
                        // nothing
                    }
                }
            }
        }

    override fun stop() {
        if (::encoder.isInitialized) {
            encoder.pixel.off()
        }
    }
}
