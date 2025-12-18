package com.gradle.projectreport.functional

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ExtensionConfigurationTest {

    @TempDir
    lateinit var projectDir: File

    @Test
    fun `extension renderDependencies controls dependency rendering`() {
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
            }
            """
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport", "--stacktrace")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":projectReport")?.outcome)

        val reportFile = File(projectDir, "build/reports/project-report.md")
        val content = reportFile.readText()

        assertTrue(content.contains("# Project Report"))
        assertFalse(content.contains("## Dependencies"), "Should not render dependencies when disabled")
        assertFalse(content.contains("commons-lang3"), "Should not contain any dependency information")
    }

    @Test
    fun `extension output controls output location`() {
        setupProject(
            """
            plugins {
                id("com.gradle.project-report")
            }

            projectReport {
                output.set(layout.buildDirectory.file("custom/my-report.md"))
            }
            """
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":projectReport")?.outcome)

        val customReportFile = File(projectDir, "build/custom/my-report.md")
        assertTrue(customReportFile.exists(), "Report should be created at custom location")

        val defaultReportFile = File(projectDir, "build/reports/project-report.md")
        assertFalse(defaultReportFile.exists(), "Report should not be created at default location")
    }

    @Test
    fun `extension output can use absolute path`() {
        val customOutputDir = File(projectDir, "custom-output")
        customOutputDir.mkdirs()

        setupProject(
            """
            plugins {
                id("com.gradle.project-report")
            }

            projectReport {
                output.set(file("${customOutputDir.absolutePath.replace("\\", "/")}/report.md"))
            }
            """
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":projectReport")?.outcome)

        val reportFile = File(customOutputDir, "report.md")
        assertTrue(reportFile.exists(), "Report should be created at absolute path")
    }

    @Test
    fun `extension uses default values when not configured`() {
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

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":projectReport")?.outcome)

        // Default output location
        val reportFile = File(projectDir, "build/reports/project-report.md")
        assertTrue(reportFile.exists(), "Report should be created at default location")

        // Default renderDependencies = true
        val content = reportFile.readText()
        assertTrue(content.contains("## Dependencies"), "Should render dependencies by default")
    }

    @Test
    fun `extension can be configured with convention methods`() {
        setupProject(
            """
            plugins {
                id("com.gradle.project-report")
            }

            projectReport {
                renderDependencies.convention(false)
                output.convention(layout.buildDirectory.file("reports/custom-report.md"))
            }
            """
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":projectReport")?.outcome)

        val reportFile = File(projectDir, "build/reports/custom-report.md")
        assertTrue(reportFile.exists(), "Report should be created at configured location")

        val content = reportFile.readText()
        assertFalse(content.contains("## Dependencies"), "Should respect convention configuration")
    }

    @Test
    fun `extension configuration affects task caching`() {
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
                renderDependencies.set(true)
            }
            """
        )

        // First run
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport")
            .withPluginClasspath()
            .build()

        // Change extension configuration
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
            }
            """
        )

        // Second run should not be up-to-date
        val secondResult = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, secondResult.task(":projectReport")?.outcome)

        val reportFile = File(projectDir, "build/reports/project-report.md")
        val content = reportFile.readText()
        assertFalse(content.contains("## Dependencies"))
    }

    @Test
    fun `changing output location creates new file and task is not up-to-date`() {
        setupProject(
            """
            plugins {
                id("com.gradle.project-report")
            }

            projectReport {
                output.set(layout.buildDirectory.file("reports/first.md"))
            }
            """
        )

        // First run
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport")
            .withPluginClasspath()
            .build()

        val firstFile = File(projectDir, "build/reports/first.md")
        assertTrue(firstFile.exists())

        // Change output location
        setupProject(
            """
            plugins {
                id("com.gradle.project-report")
            }

            projectReport {
                output.set(layout.buildDirectory.file("reports/second.md"))
            }
            """
        )

        // Second run
        val secondResult = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, secondResult.task(":projectReport")?.outcome)

        val secondFile = File(projectDir, "build/reports/second.md")
        assertTrue(secondFile.exists(), "New output file should be created")
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
