dependencies {
    implementation(project(":modules:utils"))
    implementation(project(":modules:event"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}