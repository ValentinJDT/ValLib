val shadowJars = mutableListOf<Dependency?>();
dependencies {
    implementation(project(":utils"))
    implementation(project(":event"))
    testImplementation(kotlin("test"))
    shadowJars.add(implementation("org.bspfsystems:yamlconfiguration:3.0.3"))
}

repositories {
    maven {
        name = "Sonatype Releases"
        url = uri("https://oss.sonatype.org/content/repositories/releases/")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    configurations["compileClasspath"].filter { file: File ->
        shadowJars.any { shadowJar -> shadowJar?.let { file.name.contains(shadowJar.name) } ?: false }
    }.forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}