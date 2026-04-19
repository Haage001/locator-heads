import java.util.Properties

plugins {
    //? if >=26.1
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
    //? if <=1.21.11
    /*id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"*/
    id("maven-publish")
    id("com.modrinth.minotaur") version "2.8.7"
}

val targetVersion = (System.getenv("STONECUTTER_ACTIVE") ?: project.property("minecraft_version")).toString()
version = "${project.property("mod_version")}+${targetVersion}"
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

repositories {
    maven("https://maven.terraformersmc.com/") { name = "TerraformersMC" }
    maven("https://maven.shedaniel.me/") { name = "Shedaniel" }
}

fabricApi {
    configureDataGeneration {
        client = true
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${project.property("fabric_loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_api_version")}")

    compileOnly("me.shedaniel.cloth:cloth-config-fabric:${project.property("cloth_config_version")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    compileOnly("com.terraformersmc:modmenu:${project.property("modmenu_version")}")
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        val targetVersionStr = targetVersion.toString()
        
        // Directly load the variant's properties file from disk for absolute accuracy
        val propsFile = rootProject.file("versions/$targetVersionStr/gradle.properties")
        val variantProps = Properties()
        if (propsFile.exists()) {
            propsFile.inputStream().use { stream ->
                variantProps.load(stream)
            }
        }

        // Helper to get property from variant file or fallback to project properties
        fun getProp(key: String, fallback: String): String {
            return variantProps.getProperty(key) ?: project.findProperty(key)?.toString() ?: fallback
        }

        val loaderVer = getProp("fabric_loader_version", "0.17.0")
        val clothVer = getProp("cloth_config_version", "20.0.0")
        val modmenuVer = getProp("modmenu_version", "16.0.0")

        val mcDep = if (targetVersionStr.startsWith("26")) ">=26.1" else ">=1.21"
        val javaDep = if (targetVersionStr.startsWith("26")) ">=25" else ">=21"

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

tasks.withType<JavaCompile>().configureEach {
    //? if >=26.1
    options.release.set(25)
    //? if <=1.21.11
    /*options.release.set(21)*/
}

java {
    withSourcesJar()
    
    //? if >=26.1
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    //? if <=1.21.11
    /*toolchain.languageVersion.set(JavaLanguageVersion.of(21))*/
}

tasks.jar {
    inputs.property("archivesName", project.base.archivesName)
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${inputs.properties["archivesName"]}" }
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("locator-heads")
    versionNumber.set("${project.property("mod_version")}+${targetVersion}")
    versionType.set("release")
    
    // Safely retrieve remapJar, as it may not be present during initial chiseledSetup config phases
    afterEvaluate {
        tasks.findByName("remapJar")?.let {
            uploadFile.set(it)
        }
    }
    
    gameVersions.add(targetVersion as String)
    //? if <=1.21.7
    /*gameVersions.add("1.21.8")*/
    loaders.add("fabric")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name") as String
            from(components["java"])
        }
    }
}
