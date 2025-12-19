plugins {
    kotlin("jvm")
    id("com.gradle.project-report")
}

group = "com.example"
description = "Core library with business logic"

dependencies {
    // Project dependency
    implementation(project(":lib-utils"))

    // External dependencies
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// Configure the project report plugin
projectReport {
    renderDependencies.set(true)
    output.set(layout.buildDirectory.file("reports/lib-core-report.md"))
}
