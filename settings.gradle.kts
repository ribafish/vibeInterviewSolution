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
        termsOfUseUrl.set("https://gradle.com/terms-of-service")
        termsOfUseAgree.set("yes")
        uploadInBackground.set(false)
    }
}

rootProject.name = "project-report-plugin"
