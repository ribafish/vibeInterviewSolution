package com.example

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ProjectReportPluginTest {

    @TempDir
    lateinit var testProjectDir: File
    private lateinit var buildFile: File

    @BeforeEach
    fun setup() {
        buildFile = File(testProjectDir, "build.gradle.kts")
        buildFile.writeText(
            """
            plugins {
                id("com.example.project-report")
            }

            version = "1.0.0"
            group = "com.mycompany"
            description = "My test project"
            """.trimIndent()
        )
    }

    private fun createRunner(vararg arguments: String): GradleRunner {
        return GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments(*arguments)
            .withDebug(true)
            .forwardOutput()
    }

    @Test
    fun `plugin applies successfully and registers projectReport task`() {
        val result = try {
            createRunner("tasks", "--all", "--dry-run").build()
        } catch (e: Exception) {
            if (e is org.gradle.testkit.runner.UnexpectedBuildFailure) {
                println("TestKit build output:\n${e.buildResult.output}")
            }
            throw e
        }

        assertTrue(result.output.contains("projectReport"), "Expected projectReport task to be registered")
    }

    @Test
    fun `projectReport task runs successfully and creates report file`() {
        val result = try {
            createRunner("projectReport").build()
        } catch (e: Exception) {
            if (e is org.gradle.testkit.runner.UnexpectedBuildFailure) {
                println("TestKit build output:\n${e.buildResult.output}")
            }
            throw e
        }

        assertTrue(result.output.contains("Project report generated at:"), "Expected report generation message")
        val reportFile = File(testProjectDir, "build/reports/project-report.md")
        assertTrue(reportFile.exists(), "Expected report file to exist")
        assertTrue(reportFile.readText().contains("# Project: testProject"), "Expected report to contain project name")
        assertTrue(reportFile.readText().contains("**Group:** com.mycompany"), "Expected report to contain project group")
        assertTrue(reportFile.readText().contains("**Description:** My test project"), "Expected report to contain project description")
    }

    @Test
    fun `projectReport task does not render dependencies when renderDependencies is false`() {
        buildFile.writeText(
            """
            plugins {
                id("com.example.project-report")
            }

            version = "1.0.0"
            group = "com.mycompany"
            description = "My test project"

            projectReport {
                renderDependencies = false
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                implementation("org.apache.commons:commons-lang3:3.12.0")
            }
            """.trimIndent()
        )

        val result = try {
            createRunner("projectReport").build()
        } catch (e: Exception) {
            if (e is org.gradle.testkit.runner.UnexpectedBuildFailure) {
                println("TestKit build output:\n${e.buildResult.output}")
            }
            throw e
        }

        val reportFile = File(testProjectDir, "build/reports/project-report.md")
        val reportContent = reportFile.readText()

        assertFalse(reportContent.contains("Configurations and Dependencies"), "Expected dependencies section not to be rendered")
        assertFalse(reportContent.contains("commons-lang3"), "Expected commons-lang3 not to be in report")
    }

    @Test
    fun `projectReport task writes to custom output path`() {
        val customOutputPath = "custom-reports/my-project-report.md"
        buildFile.writeText(
            """
            plugins {
                id("com.example.project-report")
            }

            version = "1.0.0"
            group = "com.mycompany"
            description = "My test project"

            projectReport {
                output = file("$customOutputPath")
            }
            """.trimIndent()
        )

        val result = try {
            createRunner("projectReport").build()
        } catch (e: Exception) {
            if (e is org.gradle.testkit.runner.UnexpectedBuildFailure) {
                println("TestKit build output:\n${e.buildResult.output}")
            }
            throw e
        }

        val reportFile = File(testProjectDir, customOutputPath)
        assertTrue(reportFile.exists(), "Expected report file to exist at custom path")
        assertTrue(reportFile.readText().contains("# Project: testProject"), "Expected report to contain project name")
    }

    @Test
    fun `projectReport task renders dependencies correctly`() {
        buildFile.writeText(
            """
            plugins {
                id("com.example.project-report")
                id("java-library")
            }

            version = "1.0.0"
            group = "com.mycompany"
            description = "My test project"

            projectReport {
                renderDependencies = true
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                implementation("org.apache.commons:commons-lang3:3.12.0")
            }
            """.trimIndent()
        )

        val result = try {
            createRunner("projectReport", "--stacktrace").build()
        } catch (e: Exception) {
            if (e is org.gradle.testkit.runner.UnexpectedBuildFailure) {
                println("TestKit build output:\n${e.buildResult.output}")
            }
            throw e
        }

        val reportFile = File(testProjectDir, "build/reports/project-report.md")
        assertTrue(reportFile.exists(), "Expected report file to exist")
        val reportContent = reportFile.readText()

        assertTrue(reportContent.contains("## Configurations and Dependencies"), "Expected dependencies section to be rendered")
        assertTrue(reportContent.contains("### Configuration: implementation"), "Expected implementation configuration to be listed")
        assertTrue(reportContent.contains("org.apache.commons:commons-lang3:3.12.0 - commons-lang3-3.12.0.jar"), "Expected commons-lang3 dependency to be in report with correct format")
    }
}