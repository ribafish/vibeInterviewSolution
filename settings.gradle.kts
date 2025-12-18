plugins {
    id("com.gradle.develocity") version "3.18.2"
}

rootProject.name = "project-report-plugin"

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
        publishing.onlyIf { true }
    }
}
