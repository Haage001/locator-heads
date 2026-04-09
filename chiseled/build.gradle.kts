plugins {
    //? if >=26.1
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
    //? if <=1.21.11
    /*id("net.fabricmc.fabric-loom-remap") version "1.15-SNAPSHOT"*/
    id("maven-publish")
    id("com.modrinth.minotaur") version "2.8.7"
}

version = project.property("mod_version") as String
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
    implementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_api_version")}")

    compileOnly("me.shedaniel.cloth:cloth-config-fabric:${project.property("cloth_config_version")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    compileOnly("com.terraformersmc:modmenu:${project.property("modmenu_version")}")
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(mutableMapOf("version" to project.version))
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
    versionNumber.set("${project.property("mod_version")}+${project.property("minecraft_version")}")
    versionType.set("release")
    
    // Safely retrieve remapJar, as it may not be present during initial chiseledSetup config phases
    afterEvaluate {
        tasks.findByName("remapJar")?.let {
            uploadFile.set(it)
        }
    }
    
    gameVersions.add(project.property("minecraft_version") as String)
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
