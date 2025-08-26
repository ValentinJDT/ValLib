dependencies {
    implementation(project(":utils"))
    implementation(project(":event"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}