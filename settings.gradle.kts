pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.gradle.develocity") version "4.3"
}

val publishBuildScans = providers.environmentVariable("DISABLE_BUILD_SCAN")
    .map { it.equals("true", ignoreCase = true) }
    .orElse(false)
    .map { disabled -> !disabled }
    .get()

develocity {
    server = "https://scans.gradle.com"
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
        publishing.onlyIf { publishBuildScans }
        uploadInBackground = false
    }
}

rootProject.name = "project-report-plugin"
