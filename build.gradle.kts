import org.gradle.api.tasks.bundling.Zip

plugins {
    kotlin("jvm") version "1.8.22"
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
    implementation(gradleApi())

    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Zip>("packageProject") {
    archiveFileName.set("project.zip")
    destinationDirectory.set(layout.projectDirectory)
    from(layout.projectDirectory) {
        include("**/*")
        exclude(
            "build/**",
            ".gradle/**",
            "**/.gradle/**",
            "**/.idea/**",
            ".git/**",
            "**/.DS_Store",
            ".venv-codex/**",
            "project.zip"
        )
    }
}
