plugins {
    id("java")
    id("java-library")
    kotlin("jvm") version("1.8.0")

    id("dev.architectury.loom") version("1.1-SNAPSHOT") apply false
    id("architectury-plugin") version("3.4-SNAPSHOT") apply false
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    version = project.properties["mod_version"]!!
    group = project.properties["maven_group"]!!

    repositories {
        mavenCentral()
        maven(url = "https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
        maven("https://maven.impactdev.net/repository/development/")
    }

    java {
        withSourcesJar()
    }
}
