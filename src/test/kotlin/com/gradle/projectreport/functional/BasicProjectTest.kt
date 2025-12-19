package com.gradle.projectreport.functional

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class BasicProjectTest {

    @TempDir
    lateinit var projectDir: File

    @Test
    fun `generates report for empty project`() {
        setupProject(
            """
            plugins {
                id("com.gradle.project-report")
            }

            group = "com.example"
            version = "1.0.0"
            description = "Test project"
            """
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport", "--stacktrace")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":projectReport")?.outcome)

        val reportFile = File(projectDir, "build/reports/project-report.md")
        assertTrue(reportFile.exists(), "Report file should be created")

        val content = reportFile.readText()
        assertTrue(content.contains("# test-project"))
        assertTrue(content.contains("**Name**: test-project"))
        assertTrue(content.contains("**Group**: com.example"))
        assertTrue(content.contains("**Description**: Test project"))
        assertFalse(content.contains("## Dependencies"), "Empty project should not have dependencies section")
    }

    @Test
    fun `task is up-to-date when run twice without changes`() {
        setupProject(
            """
            plugins {
                id("com.gradle.project-report")
            }
            """
        )

        // First run
        val firstResult = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, firstResult.task(":projectReport")?.outcome)

        // Second run
        val secondResult = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.UP_TO_DATE, secondResult.task(":projectReport")?.outcome)
    }

    @Test
    fun `task is not up-to-date when project metadata changes`() {
        setupProject(
            """
            plugins {
                id("com.gradle.project-report")
            }

            group = "com.example"
            """
        )

        // First run
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport")
            .withPluginClasspath()
            .build()

        // Change project metadata
        setupProject(
            """
            plugins {
                id("com.gradle.project-report")
            }

            group = "com.example.changed"
            """
        )

        // Second run
        val secondResult = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, secondResult.task(":projectReport")?.outcome)

        val reportFile = File(projectDir, "build/reports/project-report.md")
        val content = reportFile.readText()
        assertTrue(content.contains("**Group**: com.example.changed"))
    }

    @Test
    fun `task works with build cache`() {
        setupProject(
            """
            plugins {
                id("com.gradle.project-report")
            }

            group = "com.example"
            """
        )

        val buildCacheDir = File(projectDir, ".gradle-cache")

        // First run with cache enabled
        val firstResult = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport", "--build-cache")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, firstResult.task(":projectReport")?.outcome)

        // Delete output
        val reportFile = File(projectDir, "build/reports/project-report.md")
        reportFile.delete()

        // Second run should restore from cache
        val secondResult = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport", "--build-cache")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.FROM_CACHE, secondResult.task(":projectReport")?.outcome)
        assertTrue(reportFile.exists(), "Report should be restored from cache")
    }

    @Test
    fun `handles project without group or description`() {
        setupProject(
            """
            plugins {
                id("com.gradle.project-report")
            }
            """
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":projectReport")?.outcome)

        val reportFile = File(projectDir, "build/reports/project-report.md")
        val content = reportFile.readText()
        assertTrue(content.contains("**Name**: test-project"))
        assertFalse(content.contains("**Group**"))
        assertFalse(content.contains("**Description**"))
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
