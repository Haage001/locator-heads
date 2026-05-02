// ┌──────────────────────────────────────────────────────────────────────┐
// │  build.gradle.kts — Unified Build Script                             │
// │  Uses Stonecutter syntax to branch between supported versions        │
// └──────────────────────────────────────────────────────────────────────┘

plugins {
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT" apply false
    id("net.fabricmc.fabric-loom-remap") version "1.15-SNAPSHOT" apply false
    id("maven-publish")
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}

val targetVersion = sc.current.version
val isModern = targetVersion.startsWith("26")
val javaVer = if (isModern) 25 else 21

if (isModern) {
    apply(plugin = "net.fabricmc.fabric-loom")
} else {
    apply(plugin = "net.fabricmc.fabric-loom-remap")
}

version = "${property("mod_version")}+${targetVersion}"
group = property("maven_group") as String

base {
    archivesName.set(property("archives_base_name") as String)
}

repositories {
    maven("https://maven.terraformersmc.com/") { name = "TerraformersMC" }
    maven("https://maven.shedaniel.me/") { name = "Shedaniel" }
}

val loom = project.extensions.getByName<net.fabricmc.loom.api.LoomGradleExtensionAPI>("loom")

dependencies {
    "minecraft"("com.mojang:minecraft:${property("minecraft_version")}")
    
    if (isModern) {
        implementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
        implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    } else {
        @Suppress("UnstableApiUsage")
        "mappings"(loom.officialMojangMappings())
        "modImplementation"("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
        "modImplementation"("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    }

    if (isModern) {
        compileOnly("me.shedaniel.cloth:cloth-config-fabric:${property("cloth_config_version")}") {
            exclude(group = "net.fabricmc.fabric-api")
        }
        compileOnly("com.terraformersmc:modmenu:${property("modmenu_version")}")
    } else {
        "modCompileOnly"("me.shedaniel.cloth:cloth-config-fabric:${property("cloth_config_version")}") {
            exclude(group = "net.fabricmc.fabric-api")
        }
        "modCompileOnly"("com.terraformersmc:modmenu:${property("modmenu_version")}")
    }
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        val loaderVer = project.property("fabric_loader_version").toString()
        val clothVer  = project.property("cloth_config_version").toString()
        val modmenuVer = project.property("modmenu_version").toString()
        val mcDep = findProperty("minecraft_dependency")?.toString() ?: ">=26.1"

        expand(mutableMapOf(
            "version" to project.version,
            "minecraft_dependency" to mcDep,
            "java_dependency" to if (isModern) ">=25" else ">=21",
            "fabric_loader_dependency" to ">=$loaderVer",
            "cloth_config_dependency" to ">=$clothVer",
            "modmenu_dependency" to ">=$modmenuVer"
        ))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaVer)
}

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVer))
}

tasks.jar {
    inputs.property("archivesName", project.base.archivesName)
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${inputs.properties["archivesName"]}" }
    }
}

publishMods {
    file = tasks.jar.flatMap { it.archiveFile }
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = STABLE
    modLoaders.add("fabric")

    // Read compatible versions from versions/*/gradle.properties
    // When a new hotfix drops, just add it to the comma-separated list there
    val compatibleVersions = (property("compatible_versions") as String).split(",")

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = "locator-heads"
        minecraftVersions.addAll(compatibleVersions)
    }
}

afterEvaluate {
    tasks.findByName("remapJar")?.let { remapTask ->
        publishMods {
            file = (remapTask as org.gradle.jvm.tasks.Jar).archiveFile
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = property("archives_base_name") as String
            from(components["java"])
        }
    }
}
