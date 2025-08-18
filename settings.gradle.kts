pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "ValLib"

File(rootDir, "modules").listFiles().filter { it.isDirectory }.forEach { module ->
    include(":modules:${module.name}")
}