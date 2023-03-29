import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    silentMojangMappingsLicense()
    enableTransitiveAccessWideners.set(true)
}
val shadowCommon = configurations.create("shadowCommon")
dependencies {
    minecraft("com.mojang:minecraft:1.19.2")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.14.14")

    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:0.75.1+1.19.2")
    modRuntimeOnly("dev.architectury", "architectury-fabric", "6.5.69")
    implementation(project(":common", configuration = "namedElements"))
    "developmentFabric"(project(":common", configuration = "namedElements"))

    modImplementation("com.cobblemon:fabric:1.3.1+1.19.2-SNAPSHOT")
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
        exclude("architectury.common.json")
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


