pluginManagement {
    includeBuild("../..")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "project-report-example"

include("app", "library", "utils")

plugins {
    id("com.gradle.develocity") version "4.3"
}

develocity {
    server.set("https://scans.gradle.com")
    buildScan {
        termsOfUseUrl.set("https://gradle.com/terms-of-service")
        termsOfUseAgree.set("yes")
        publishing.onlyIf { true }
    }
}
