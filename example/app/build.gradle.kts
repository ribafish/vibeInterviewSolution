plugins {
    kotlin("jvm")
    application
    id("com.gradle.project-report")
}

group = "com.example"
description = "Main application module"

application {
    mainClass.set("com.example.app.MainKt")
}

dependencies {
    // Project dependencies
    implementation(project(":lib-core"))
    implementation(project(":lib-utils"))

    // External dependencies
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("io.ktor:ktor-server-core:2.3.5")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// Configure the project report plugin with custom output
projectReport {
    renderDependencies.set(true)
    output.set(layout.buildDirectory.file("reports/app-dependencies-report.md"))
}
