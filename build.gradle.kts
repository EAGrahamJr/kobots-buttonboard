plugins {
    application
    kotlin("jvm") version "1.8.21"
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
val JAR_NAME = "bb-app"

dependencies {
    implementation("com.diozero:diozero-core:$DIOZERO_VER")
    implementation("crackers.kobots:kobots-devices:0.0.6")
//    implementation("com.diozero:diozero-provider-pigpio:$DIOZERO_VER")
//    implementation("com.diozero:diozero-provider-remote:$DIOZERO_VER")
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
