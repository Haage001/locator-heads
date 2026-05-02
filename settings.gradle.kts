pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.3"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Read supported versions from root gradle.properties (single source of truth)
val stonecutterVersions = (providers.gradleProperty("stonecutter_versions").getOrNull() ?: "26.1")
    .split(",")
    .map { it.trim() }

stonecutter {
    create(rootProject) {
        versions(stonecutterVersions)
        vcsVersion = "26.1"
    }
}

rootProject.name = "locator-heads"
