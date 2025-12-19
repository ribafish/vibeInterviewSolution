plugins {
    id("com.gradle.develocity") version "4.3"
}

rootProject.name = "example-application"

includeBuild("..")

include("app")
include("lib-core")
include("lib-utils")

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
        publishing.onlyIf { true }
    }
}
