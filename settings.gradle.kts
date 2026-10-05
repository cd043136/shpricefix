rootProject.name = "shpricefix"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }

    val loom_version: String by settings

    plugins {
        id("net.fabricmc.fabric-loom") version loom_version
    }
}
