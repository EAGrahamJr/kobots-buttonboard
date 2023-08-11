package crackers.kobots.buttonboard

import org.eclipse.paho.mqttv5.client.*
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.MqttSubscription
import org.eclipse.paho.mqttv5.common.packet.MqttProperties
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Constains MQTT shite
 */
private const val BROKER = "tcp://192.168.1.4:1883"
val mqttClient by lazy {
    val options = MqttConnectionOptions().apply {
        isAutomaticReconnect = true
        isCleanStart = true
        keepAliveInterval = 10
        connectionTimeout = 10
    }
    MqttAsyncClient(BROKER, "buttonboard", MemoryPersistence()).apply {
        setCallback(mqttCallback)
        connect(options).waitForCompletion()
    }
}

private val mqttCallback = object : MqttCallback {
    private val logger = LoggerFactory.getLogger("MQTTCallback")
    override fun disconnected(disconnectResponse: MqttDisconnectResponse) {
        logger.error("Disconnected: $disconnectResponse")
    }

    override fun mqttErrorOccurred(exception: MqttException) {
        logger.error("Error: $exception")
    }

    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        logger.warn(if (reconnect) "Re-connected" else "Connected")
    }

    override fun authPacketArrived(reasonCode: Int, properties: MqttProperties?) {
        // don't care?
    }

    override fun messageArrived(topic: String, message: MqttMessage) {
        // just use the listener
    }

    override fun deliveryComplete(token: IMqttToken?) {
        // don't care?
    }
}

private const val KOBOTS_ALIVE = "kobots/alive"
private val executor = Executors.newSingleThreadScheduledExecutor()

fun startAliveCheckSub() {
    val kobotsAliveSub = MqttSubscription(KOBOTS_ALIVE, 0)
    // STUPID FUCKING BUG!!!!
    val props = MqttProperties().apply {
        subscriptionIdentifiers = listOf(0)
    }
    mqttClient.subscribe(arrayOf(kobotsAliveSub), null, null, aliveHandler, props).waitForCompletion()
    executor.scheduleAtFixedRate(aliveChecker, 2, 2, TimeUnit.MINUTES)
}

fun stopAliveCheckSub() {
    executor.shutdownNow()
}

private val lastCheckIn = mutableMapOf<String, LocalTime>()

private val liveLogger = LoggerFactory.getLogger("AliveHandler")
private val aliveHandler = object : IMqttMessageListener {
    override fun messageArrived(topic: String, message: MqttMessage) {
        val whoIsAlive = message.payload.decodeToString()
//        liveLogger.info("Kobots alive: $whoIsAlive")
        lastCheckIn[whoIsAlive] = LocalTime.now()
    }
}

private val aliveChecker = Runnable {
    val now = LocalTime.now()
    lastCheckIn.forEach { host, timeCheck ->
        if (Duration.between(timeCheck, now).toSeconds() > 90) liveLogger.error("$host last seen $timeCheck")
    }
}
