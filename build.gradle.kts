plugins {
    kotlin("jvm") version "1.9.24"
    `java-gradle-plugin`
}

group = "com.vibe"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    plugins {
        create("projectReport") {
            id = "com.vibe.projectreport"
            implementationClass = "com.vibe.projectreport.ProjectReportPlugin"
            displayName = "Project Report Plugin"
            description = "Generates a Markdown report for a Gradle project."
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
