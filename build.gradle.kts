plugins {
    id("com.example.project-report")
    id("java-library") // Corrected line
}

group = "com.mycompany.example"
version = "0.1.0"
description = "A sample project to test the report plugin"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.12.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
}

// Configure the projectReport extension (optional, demonstrating usage)
projectReport {
    renderDependencies = true
    output = layout.buildDirectory.file("custom-reports/my-project-report-from-root.md")
}
