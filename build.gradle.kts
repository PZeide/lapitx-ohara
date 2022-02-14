plugins {
    id("fabric-loom") version "0.11-SNAPSHOT"
}

val mavenGroup: String by project
val modVersion: String by project
val modId: String by project

val minecraftVersion: String by project
val yarnMappings: String by project
val loaderVersion: String by project
val fabricVersion: String by project

group = mavenGroup
version = modVersion

base {
    archivesName.set(modId)
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnMappings:v2")
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
}

tasks {
    val javaVersion = JavaVersion.VERSION_17

    compileJava {
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.encoding = "UTF-8"
        options.release.set(javaVersion.toString().toInt())
    }

    java {
        withSourcesJar()
    }

    jar {
        from("LICENSE") { rename { "${it}_${base.archivesName}" } }
    }

    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(mapOf(
                "version" to project.version,
                "mod_id" to modId,
                "java_version" to javaVersion.toString(),
                "fabric_loader" to loaderVersion,
                "minecraft_version" to minecraftVersion
            ))
        }
    }
}