plugins {
    id("com.vibe.projectreport")
    id("java")
}

group = "com.example.demo"
version = "1.0.0"
description = "Aggregated report across all subprojects."

repositories {
    mavenCentral()
}

subprojects {
    group = rootProject.group
    version = rootProject.version
    repositories {
        mavenCentral()
    }
}

dependencies {
    implementation(project(":app"))
    implementation(project(":library"))
    implementation(project(":utils"))
}
