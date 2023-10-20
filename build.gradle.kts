plugins {
    kotlin("jvm") version "1.8.20"
    id("maven-publish")
}

group = "fr.valentin.lib"
version = "0.1.8"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group as String
            artifactId = rootProject.name
            version = version

            from(components["java"])
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
