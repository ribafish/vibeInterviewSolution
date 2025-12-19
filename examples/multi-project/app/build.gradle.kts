plugins {
    id("java")
}

description = "Example application using the project report plugin."

dependencies {
    implementation(project(":library"))
    implementation("org.apache.commons:commons-text:1.10.0")
}
