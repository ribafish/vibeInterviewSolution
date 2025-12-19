plugins {
    kotlin("jvm") version "1.9.22" apply false
    id("com.gradle.project-report")
}

group = "com.example"
version = "1.0.0"
description = "Example multi-project application"

allprojects {
    repositories {
        mavenCentral()
    }
}

// Configure the project report plugin for multi-project report
projectReport {
    renderDependencies.set(true)
    output.set(layout.buildDirectory.file("reports/multi-project-report.md"))
}
