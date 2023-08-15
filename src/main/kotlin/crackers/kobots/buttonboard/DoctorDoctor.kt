package crackers.kobots.buttonboard

import crackers.kobots.mqtt.KobotsMQTT
import crackers.kobots.mqtt.KobotsMQTT.Companion.BROKER
import crackers.kobots.mqtt.KobotsMQTT.Companion.KOBOTS_ALIVE
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Contains MQTT shite and maintains the "alive-check" subscription for all Kobots systems.
 */
object DoctorDoctor {
    private val logger = LoggerFactory.getLogger("DoctorDoctor")

    private val lastCheckIn = mutableMapOf<String, LocalTime>()

    private val client by lazy { KobotsMQTT("buttonboard", BROKER, MemoryPersistence()) }
    private val executor by lazy { Executors.newSingleThreadScheduledExecutor() }

    fun start() {
        with(client) {
            subscribe(KOBOTS_ALIVE) { whoIsAlive -> lastCheckIn[whoIsAlive] = LocalTime.now() }
        }
        executor.scheduleAtFixedRate(aliveChecker, 15, 15, TimeUnit.SECONDS)
    }

    fun stop() {
        executor.shutdownNow()
    }

    private val aliveChecker = Runnable {
        val now = LocalTime.now()
        lastCheckIn.forEach { host, timeCheck ->
            if (Duration.between(timeCheck, now).toSeconds() > 90) logger.error("$host last seen $timeCheck")
        }
    }
}
