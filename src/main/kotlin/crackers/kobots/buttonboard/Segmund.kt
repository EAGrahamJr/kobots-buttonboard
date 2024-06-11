package crackers.kobots.buttonboard

import crackers.kobots.app.AppCommon
import crackers.kobots.devices.display.HT16K33.Companion.DEFAULT_I2C_ADDRESS
import crackers.kobots.devices.display.QwiicAlphanumericDisplay
import crackers.kobots.parts.elapsed
import crackers.kobots.parts.scheduleAtRate
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.seconds

/**
 * TODO fill this in
 */
object Segmund : AppCommon.Startable {
    private val logger = LoggerFactory.getLogger("Segmund")
    private lateinit var display: QwiicAlphanumericDisplay

    private lateinit var future: Future<*>

    private enum class Mode {
        OFF,
        CLOCK,
    }

    private val mode = AtomicReference(Mode.OFF)
    private val CLOCK_ON = Duration.ofSeconds(15)
    private var clockTime = Instant.EPOCH

    override fun start() {
        val address = DEFAULT_I2C_ADDRESS + 1
        display =
            QwiicAlphanumericDisplay(listOf(i2cMultiplexer.getI2CDevice(4, address))).apply {
                brightness = 0.06f
                autoShow = true
            }
        var colon = true
        val lastMode = AtomicReference(Mode.OFF)

        future =
            AppCommon.executor.scheduleAtRate(1.seconds) {
                AppCommon.whileRunning {
                    if (lastMode.get() != mode.get()) {
                        lastMode.set(mode.get())
                        logger.info("Mode change: {}", mode.get())
                    }
                    when (mode.get()) {
                        Mode.CLOCK ->
                            if (clockTime.elapsed() < CLOCK_ON) {
                                with(display) {
                                    if (!on) on = true
                                    clock(LocalTime.now(), colon)
                                    colon = !colon
                                }
                            } else {
                                mode.set(Mode.OFF)
                            }

                        Mode.OFF ->
                            if (display.on) {
                                display.on = false
                                display.fill(false)
                            }

                        else -> {}
                    }
                }
            }
    }

    fun showTime() {
        if (mode.compareAndSet(Mode.OFF, Mode.CLOCK)) clockTime = Instant.now()
    }

    override fun stop() {
        if (::future.isInitialized) future.cancel(true)
        if (::display.isInitialized) {
            display.apply {
                fill(false)
                on = false
            }
        }
    }
}
