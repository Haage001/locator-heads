pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
	id("dev.kikugie.stonecutter") version "0.9"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        versions("1.21.10", "1.21.11", "26.1", "26.2")
        vcsVersion = System.getenv("STONECUTTER_ACTIVE") ?: "26.1"
    }
}

rootProject.name = "locator-heads"
include("chiseled")
