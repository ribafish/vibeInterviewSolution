pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.gradle.develocity") version "4.3"
}

develocity {
    server.set("https://scans.gradle.com")
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
        isUploadInBackground = false
    }
}

rootProject.name = "project-report-plugin"
