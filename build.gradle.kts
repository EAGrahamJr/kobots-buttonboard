plugins {
    application
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jmailen.kotlinter") version "4.1.1"
}

group = "crackers.kobots"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

val DIOZERO_VER = "1.4.0"

val DEVICES_VER = "0.2+"
val PARTS_VER = "0.0+"
val MOPIDY_VER = "0.0+"
val HASSK_VER = "0+"

val JAR_NAME = "bboard"

dependencies {
    implementation("com.diozero:diozero-core:$DIOZERO_VER")
    implementation("org.tinylog:slf4j-tinylog:2.6.2")

    implementation("crackers.kobots:kobots-devices:$DEVICES_VER")
    implementation("crackers.kobots:kobots-parts:$PARTS_VER")
    implementation("crackers.automation:mopidy-kontrol:$MOPIDY_VER")

//    implementation("com.diozero:diozero-provider-remote:$DIOZERO_VER")
    implementation("org.eclipse.paho:org.eclipse.paho.mqttv5.client:1.2.5")
    implementation("crackers.automation:hassk:$HASSK_VER")
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
    // don't need any of these
    jar { enabled = false }
    startScripts { enabled = false }
    distTar { enabled = false }
    distZip { enabled = false }
    shadowDistTar { enabled = false }
    shadowDistZip { enabled = false }

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
