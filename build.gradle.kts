/*
 * Copyright 2022-2024 by E. A. Graham, Jr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

plugins {
    application
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jmailen.kotlinter") version "4.1.1"
}

group = "crackers.kobots"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

val DIOZERO_VER = "1.4.1"

val DEVICES_VER = "0.2+"
val PARTS_VER = "0+"
val HASSK_VER = "0+"

val JAR_NAME = "bboard"

dependencies {
    implementation("com.diozero:diozero-core:$DIOZERO_VER")
    implementation("org.tinylog:slf4j-tinylog:2.6.2")

    implementation("crackers.kobots:kobots-devices:$DEVICES_VER")
    implementation("crackers.kobots:kobots-parts:$PARTS_VER")

//    implementation("com.diozero:diozero-provider-remote:$DIOZERO_VER")
    implementation("org.eclipse.paho:org.eclipse.paho.mqttv5.client:1.2.5")
    implementation("crackers.automation:hassk:$HASSK_VER")
    implementation("com.typesafe:config:1.4.2")
}

application {
    mainClass.set("crackers.kobots.buttonboard.AppKt")
}

kotlin {
    jvmToolchain(21)
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
            val sshTarget = System.getProperty("remote", "psyche.local")
            val name = JAR_NAME

            println("Sending $name to $sshTarget")
            exec {
                commandLine(
                    "sh", "-c", """
                scp build/libs/$name.jar $sshTarget:/home/crackers
                scp *.sh $sshTarget:/home/crackers
                scp *.service $sshTarget:/home/crackers                
                """.trimIndent()
                )
            }
        }
    }
}

defaultTasks("build","deployApp")
