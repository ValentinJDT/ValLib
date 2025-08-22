plugins {
    java
    kotlin("jvm") version "2.1.10"
    `maven-publish`
}

val JDK_VERSION: Int = 17
group = property("group") as String
version = property("version") as String

repositories {
    mavenCentral()
}

val moduleNames = File(rootDir, "modules").listFiles().filter { it.isDirectory && it.name != "build" }.map { it.name }

dependencies {
    moduleNames.forEach { name ->
        implementation(project(":modules:${name}"))
    }
}

tasks.jar {
    configurations["compileClasspath"].forEach { file: File ->
        if(moduleNames.any { file.name.removeSuffix(".jar").startsWith("ValLib-${it}") }) {
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
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")

    this@subprojects.group = property("group") as String
    this@subprojects.version = property("version") as String

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