plugins {
    kotlin("jvm") version "1.9.21"
    id("maven-publish")
}

group = "fr.valentin.lib"
version = "0.2.0"

val JDK_VERSION: Int = 17

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(JDK_VERSION)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(JDK_VERSION))
    }
}
