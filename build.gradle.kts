plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.github.seggan"
version = "1.1.0"

repositories {
    mavenCentral()
}

val sqlVersion = "0.40.1"
dependencies {
    implementation("dev.kord:kord-core:0.13.1")
    implementation("io.ktor:ktor-client-java:2.3.10")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("org.slf4j:slf4j-simple:2.0.13")

    implementation("dev.reformator.stacktracedecoroutinator:stacktrace-decoroutinator-jvm:2.3.8")
}

application {
    mainClass.set("io.github.seggan.notwalshbot.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

tasks.jar {
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }
}