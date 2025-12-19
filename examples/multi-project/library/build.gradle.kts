plugins {
    id("java-library")
}

description = "Library consumed by the app."

dependencies {
    api(project(":utils"))
    implementation("org.apache.commons:commons-lang3:3.12.0")
}
