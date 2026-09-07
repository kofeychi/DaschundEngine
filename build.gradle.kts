plugins {
    kotlin("jvm") version "2.4.0"
}

group = "kofeychi"
version = "1.0-SNAPSHOT"

val lwjglVersion = "3.3.6"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("it.unimi.dsi:fastutil:8.5.18")
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    var libs = listOf(
        "lwjgl",
        "lwjgl-glfw",
        "lwjgl-opengl"
    )

    libs.forEach {
        implementation("org.lwjgl", it)
        implementation("org.lwjgl", it, classifier = "natives-windows")
    }

    implementation("org.joml:joml:1.10.8")
}

kotlin {
    jvmToolchain(26)
}