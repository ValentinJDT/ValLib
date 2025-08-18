plugins {
    java
    kotlin("jvm") version "1.9.21"
    id("maven-publish")
}

group = "fr.valentinjdt.lib"
version = property("version") as String

val JDK_VERSION: Int = 17

repositories {
    mavenCentral()
}

val moduleNames = File(rootDir, "modules").listFiles().filter { it.isDirectory }.map { it.name }

dependencies {
    moduleNames.forEach { name ->
        implementation(project(":modules:${name}"))
    }
}

tasks.jar {
    configurations["compileClasspath"].forEach { file: File ->
        if(moduleNames.contains(file.name.removeSuffix(".jar"))) {
            from(zipTree(file.absoluteFile))
        }
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

kotlin {
    jvmToolchain(JDK_VERSION)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(JDK_VERSION))
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")

    this@subprojects.group = this.group
    this@subprojects.version = this.version

    repositories {
        mavenCentral()
    }

    kotlin {
        jvmToolchain(JDK_VERSION)
    }

    tasks.jar {
        archiveBaseName = "ValLib-${project.name}"
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
}