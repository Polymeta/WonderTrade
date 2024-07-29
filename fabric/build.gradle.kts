plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    silentMojangMappingsLicense()
    enableTransitiveAccessWideners.set(true)
}

configurations.all {
    resolutionStrategy {
        force("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    }
}

val shadowCommon = configurations.create("shadowCommon")
dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")

    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    modRuntimeOnly("dev.architectury", "architectury-fabric", property("architectury_version").toString()) { isTransitive = false }
    implementation(project(":common", configuration = "namedElements"))
    "developmentFabric"(project(":common", configuration = "namedElements"))

    implementation("net.kyori:adventure-text-minimessage:${property("minimessage_version")}")
    implementation("net.kyori:adventure-text-serializer-gson:${property("minimessage_version")}")
    shadowCommon("net.kyori:adventure-text-minimessage:${property("minimessage_version")}")
    shadowCommon("net.kyori:adventure-text-serializer-gson:${property("minimessage_version")}")

    modImplementation("com.cobblemon:fabric:${property("cobblemon_version")}") { isTransitive = false }
    shadowCommon(project(":common", configuration = "transformProductionFabric"))
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(project.properties)
    }
}

tasks {

    jar {
        archiveBaseName.set("wondertrade-${project.name}")
        archiveClassifier.set("dev-slim")
    }

    shadowJar {
        exclude("architectury.common.json", "com/**/*")
        archiveClassifier.set("dev-shadow")
        archiveBaseName.set("wondertrade-${project.name}")
        configurations = listOf(shadowCommon)
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        archiveBaseName.set("wondertrade-${project.name}")
        archiveVersion.set("${rootProject.version}")
    }

}


