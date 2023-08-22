package crackers.kobots.buttonboard

import crackers.kobots.mqtt.KobotsMQTT
import crackers.kobots.mqtt.KobotsMQTT.Companion.KOBOTS_ALIVE
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Contains MQTT shite and maintains the "alive-check" subscription for all Kobots systems.
 */
object DoctorDoctor {
    private val logger = LoggerFactory.getLogger("DoctorDoctor")

    private val lastCheckIn = mutableMapOf<String, ZonedDateTime>()

    private val client by lazy { KobotsMQTT("buttonboard", "tcp://192.168.1.4:1883") }
    private val executor by lazy { Executors.newSingleThreadScheduledExecutor() }

    private val _error = AtomicReference<String>()
    var error: String?
        get() = _error.get()
        private set(value) {
            _error.set(value)
        }

    fun start() {
        with(client) {
            subscribe(KOBOTS_ALIVE) { whoIsAlive -> lastCheckIn[whoIsAlive] = ZonedDateTime.now() }
        }
        executor.scheduleAtFixedRate(aliveChecker, 15, 15, TimeUnit.SECONDS)
    }

    fun stop() {
        executor.shutdownNow()
    }

    private val aliveChecker = Runnable {
        val now = ZonedDateTime.now()
        var hasErrors = false
        lastCheckIn.forEach { host, timeCheck ->
            if (Duration.between(timeCheck, now).toSeconds() > 90) {
                logger.error("$host last seen $timeCheck")
                hasErrors = true
            }
        }
        error = if (hasErrors) "MQTT Error" else null
    }
}
