plugins {
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
    id("maven-publish")
    id("com.modrinth.minotaur") version "2.8.7"
}

// ── Stonecutter version context ──────────────────────────────────────
val targetVersion = sc.current.version
val isModern = sc.current.parsed >= "26.1"

version = "${property("mod_version")}+${targetVersion}"
group = property("maven_group") as String

base {
    archivesName.set(property("archives_base_name") as String)
}

repositories {
    maven("https://maven.terraformersmc.com/") { name = "TerraformersMC" }
    maven("https://maven.shedaniel.me/") { name = "Shedaniel" }
}

// Access the Loom extension (applied-script safe)
val loom = project.extensions.getByName<net.fabricmc.loom.api.LoomGradleExtensionAPI>("loom")

loom.apply {
    @Suppress("UnstableApiUsage")
    fabricApi {
        configureDataGeneration {
            client = true
        }
    }
}

dependencies {
    "minecraft"("com.mojang:minecraft:${property("minecraft_version")}")
    // Only obfuscated versions need Mojang mappings; Loom 1.15 treats
    // 1.21.11+ as non-obfuscated and will reject mappings for them.
    if (sc.current.parsed <= "1.21.9") {
        "mappings"(loom.officialMojangMappings())
    }
    implementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

    compileOnly("me.shedaniel.cloth:cloth-config-fabric:${property("cloth_config_version")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    compileOnly("com.terraformersmc:modmenu:${property("modmenu_version")}")
}

// ── Resource processing (property injection into fabric.mod.json) ────
tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        val loaderVer = property("fabric_loader_version").toString()
        val clothVer  = property("cloth_config_version").toString()
        val modmenuVer = property("modmenu_version").toString()

        val mcDep   = findProperty("minecraft_dependency")?.toString()
            ?: if (isModern) ">=26.1" else ">=1.21"
        val javaDep = if (isModern) ">=25" else ">=21"

        expand(mutableMapOf(
            "version" to project.version,
            "minecraft_dependency" to mcDep,
            "java_dependency" to javaDep,
            "fabric_loader_dependency" to ">=$loaderVer",
            "cloth_config_dependency" to ">=$clothVer",
            "modmenu_dependency" to ">=$modmenuVer"
        ))
    }
}

// ── Java toolchain: 26.x needs Java 25, legacy needs Java 21 ────────
tasks.withType<JavaCompile>().configureEach {
    //? if >=26.1
    options.release.set(25)
    //? if <=1.21.11
    //options.release.set(21)
}

java {
    withSourcesJar()

    //? if >=26.1
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    //? if <=1.21.11
    //toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.jar {
    inputs.property("archivesName", project.base.archivesName)
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${inputs.properties["archivesName"]}" }
    }
}

// ── Modrinth publishing ─────────────────────────────────────────────
modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("locator-heads")
    versionNumber.set("${property("mod_version")}+${targetVersion}")
    versionType.set("release")

    afterEvaluate {
        tasks.findByName("remapJar")?.let {
            uploadFile.set(it)
        }
    }

    gameVersions.add(targetVersion)
    //? if <=1.21.7
    //gameVersions.add("1.21.8")
    loaders.add("fabric")
}

// ── Maven publishing ────────────────────────────────────────────────
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = property("archives_base_name") as String
            from(components["java"])
        }
    }
}
