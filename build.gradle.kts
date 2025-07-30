/*
 * Copyright 2022-2025 by E. A. Graham, Jr.
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
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "9.0.0-rc2"
    id("org.jmailen.kotlinter") version "5.1.1"
    id("com.github.ben-manes.versions") version "0.52.0"
}

group = "crackers.kobots"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

val DIOZERO_VER = "1.4.1"

val DEVICES_VER = "0.2+"
val PARTS_VER = "0.2+"
val HASSK_VER = "0+"

val JAR_NAME = "bboard"

dependencies {
    implementation("crackers.kobots:kobots-devices:$DEVICES_VER")
    implementation("crackers.kobots:kobots-parts:$PARTS_VER")
    implementation("com.diozero:diozero-core:$DIOZERO_VER")
    implementation("crackers.automation:hassk:$HASSK_VER")

    implementation("com.typesafe:config:1.4.3")

    implementation("org.eclipse.paho:org.eclipse.paho.mqttv5.client:1.2.5")
}

application {
    mainClass.set("crackers.kobots.buttonboard.AppKt")
}

kotlin {
    jvmToolchain(21)
}

kotlinter {

}

val sshTarget = System.getProperty("remote", "psyche.local")
val shipIt: Provider<String> = providers.exec {
    commandLine(
        "sh", "-c", """
                rsync -avz build/libs/$JAR_NAME.jar $sshTarget:/home/crackers/
                rsync -avz *.sh $sshTarget:/home/crackers/
                rsync -avz *.service $sshTarget:/home/crackers/                
                """.trimIndent()
    )
}.standardError.asText

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
    register("deployApp") {
        dependsOn("shadowJar")
        doLast {
            println("Sending $JAR_NAME to $sshTarget = ${shipIt.get()}")
        }
    }
}

defaultTasks("build","deployApp")
