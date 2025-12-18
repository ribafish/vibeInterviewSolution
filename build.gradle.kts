plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

group = "com.gradle"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

gradlePlugin {
    plugins {
        create("projectReportPlugin") {
            id = "com.gradle.project-report"
            implementationClass = "com.gradle.projectreport.ProjectReportPlugin"
            displayName = "Project Report Plugin"
            description = "Generates Markdown reports with project metadata and dependency information"
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}
