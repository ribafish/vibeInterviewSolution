plugins {
    kotlin("jvm")
    id("com.gradle.project-report")
}

group = "com.example"
description = "Utility library providing common helper functions"

dependencies {
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("com.google.guava:guava:32.1.3-jre")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// Configure the project report plugin
projectReport {
    renderDependencies.set(true)
    output.set(layout.buildDirectory.file("reports/project-report.md"))
}
