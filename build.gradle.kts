plugins {
    application
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jmailen.kotlinter") version "3.12.0"
}

group = "crackers.kobots"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

val DIOZERO_VER = "1.4.0"
val JAR_NAME = "bboard"

dependencies {
    implementation("com.diozero:diozero-core:$DIOZERO_VER")
    implementation("org.tinylog:slf4j-tinylog:2.6.2")
    implementation("crackers.kobots:kobots-devices:0.1.3")
//    implementation("com.diozero:diozero-provider-remote:$DIOZERO_VER")
    implementation("org.eclipse.paho:org.eclipse.paho.mqttv5.client:1.2.5")
    implementation("crackers.automation:hassk:0.0.1")
    implementation("com.typesafe:config:1.4.2")
}

application {
    mainClass.set("crackers.kobots.buttonboard.AppKt")
}

kotlin {
    jvmToolchain(17)
}

kotlinter {
    ignoreFailures = true
    disabledRules = arrayOf("no-wildcard-imports")
}

tasks {
    check {
//        dependsOn("installKotlinterPrePushHook")
        dependsOn("formatKotlin")
    }

    /**
     * Build a "shadow jar" for single-runnable deployment
     */
    shadowJar {
        archiveBaseName.set(JAR_NAME)
        archiveVersion.set("")
        archiveClassifier.set("")
        // this is important for sing the remote client at the same time as other providers
        mergeServiceFiles()
    }

    /**
     * Deploy said shadow-jar to a remote Pi for runtime fun
     */
    create("deployApp") {
        dependsOn("shadowJar")
        doLast {
            val sshTarget = System.getProperty("remote", "useless.local")
            val name = JAR_NAME

            println("Sending $name to $sshTarget")
            exec {
                commandLine(
                    "sh", "-c", """
                scp build/libs/$name.jar $sshTarget:/home/crackers
                scp *.sh $sshTarget:/home/crackers
                """.trimIndent()
                )
            }
        }
    }
}

defaultTasks("build","deployApp")
