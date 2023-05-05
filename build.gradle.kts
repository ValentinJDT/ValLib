plugins {
    kotlin("jvm") version "1.8.20"
}

group = "fr.valentin.lib"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
}

kotlin {
    jvmToolchain(17)
}