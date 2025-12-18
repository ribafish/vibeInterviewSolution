package com.gradle.projectreport.functional

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

class GradleVersionCompatibilityTest {

    @TempDir
    lateinit var projectDir: File

    @ParameterizedTest
    @ValueSource(strings = ["7.6.4", "8.14.3", "9.2.1"])
    fun `plugin works with Gradle version`(gradleVersion: String) {
        setupProject(
            """
            plugins {
                id("com.gradle.project-report")
                `java-library`
            }

            group = "com.example"
            version = "1.0.0"
            description = "Test project for Gradle $gradleVersion"

            repositories {
                mavenCentral()
            }

            dependencies {
                implementation("org.apache.commons:commons-lang3:3.12.0")
            }
            """
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport", "--stacktrace")
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":projectReport")?.outcome,
            "Plugin should work with Gradle $gradleVersion")

        val reportFile = File(projectDir, "build/reports/project-report.md")
        assertTrue(reportFile.exists(), "Report should be created with Gradle $gradleVersion")

        val content = reportFile.readText()
        assertTrue(content.contains("# Project Report"))
        assertTrue(content.contains("**Name**: test-project"))
        assertTrue(content.contains("**Group**: com.example"))
        assertTrue(content.contains("**Description**: Test project for Gradle $gradleVersion"))
        assertTrue(content.contains("org.apache.commons:commons-lang3:3.12.0"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["7.6.4", "8.14.3", "9.2.1"])
    fun `task caching works with Gradle version`(gradleVersion: String) {
        setupProject(
            """
            plugins {
                id("com.gradle.project-report")
            }

            group = "com.example"
            """
        )

        // First run
        val firstResult = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport")
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, firstResult.task(":projectReport")?.outcome,
            "First run should succeed with Gradle $gradleVersion")

        // Second run should be up-to-date
        val secondResult = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport")
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.UP_TO_DATE, secondResult.task(":projectReport")?.outcome,
            "Second run should be up-to-date with Gradle $gradleVersion")
    }

    @ParameterizedTest
    @ValueSource(strings = ["7.6.4", "8.14.3", "9.2.1"])
    fun `extension configuration works with Gradle version`(gradleVersion: String) {
        setupProject(
            """
            plugins {
                id("com.gradle.project-report")
                `java-library`
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                implementation("org.apache.commons:commons-lang3:3.12.0")
            }

            projectReport {
                renderDependencies.set(false)
                output.set(layout.buildDirectory.file("custom/report.md"))
            }
            """
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport")
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":projectReport")?.outcome,
            "Extension configuration should work with Gradle $gradleVersion")

        val reportFile = File(projectDir, "build/custom/report.md")
        assertTrue(reportFile.exists(), "Custom output location should work with Gradle $gradleVersion")

        val content = reportFile.readText()
        assertFalse(content.contains("## Dependencies"),
            "renderDependencies=false should work with Gradle $gradleVersion")
    }

    @ParameterizedTest
    @ValueSource(strings = ["7.6.4", "8.14.3", "9.2.1"])
    fun `configuration cache works with Gradle version`(gradleVersion: String) {
        // Configuration cache requires Gradle 8.0+, so we skip for 7.x
        if (gradleVersion.startsWith("7.")) {
            return
        }

        setupProject(
            """
            plugins {
                id("com.gradle.project-report")
                `java-library`
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                implementation("org.apache.commons:commons-lang3:3.12.0")
            }
            """
        )

        // First run with configuration cache
        val firstResult = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport", "--configuration-cache")
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, firstResult.task(":projectReport")?.outcome,
            "First run with configuration cache should succeed with Gradle $gradleVersion")

        // Second run should reuse configuration cache
        val secondResult = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport", "--configuration-cache")
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .build()

        assertTrue(
            secondResult.output.contains("Reusing configuration cache") ||
            secondResult.task(":projectReport")?.outcome == TaskOutcome.UP_TO_DATE,
            "Configuration cache should be reused with Gradle $gradleVersion"
        )
    }

    private fun setupProject(buildScript: String) {
        File(projectDir, "settings.gradle.kts").writeText(
            """
            rootProject.name = "test-project"
            """.trimIndent()
        )

        File(projectDir, "build.gradle.kts").writeText(buildScript.trimIndent())
    }
}
