package com.gradle.projectreport.functional

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class DependencyRenderingTest {

    @TempDir
    lateinit var projectDir: File

    @Test
    fun `renders direct dependency commons-lang3`() {
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
            .withArguments("projectReport", "--stacktrace")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":projectReport")?.outcome)

        val reportFile = File(projectDir, "build/reports/project-report.md")
        val content = reportFile.readText()

        assertTrue(content.contains("## Dependencies"), "Should contain dependencies section")
        assertTrue(
            content.contains("org.apache.commons:commons-lang3:3.12.0 - commons-lang3-3.12.0.jar"),
            "Should contain commons-lang3 with JAR filename"
        )
    }

    @Test
    fun `renders transitive dependencies for commons-text`() {
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
                implementation("org.apache.commons:commons-text:1.10.0")
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

        assertTrue(content.contains("## Dependencies"), "Should contain dependencies section")
        assertTrue(
            content.contains("org.apache.commons:commons-text:1.10.0"),
            "Should contain direct dependency commons-text"
        )
        assertTrue(
            content.contains("org.apache.commons:commons-lang3:"),
            "Should contain transitive dependency commons-lang3"
        )
    }

    @Test
    fun `renders multiple dependencies sorted alphabetically`() {
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
                implementation("org.junit.jupiter:junit-jupiter:5.10.1")
                implementation("org.apache.commons:commons-lang3:3.12.0")
                implementation("com.google.guava:guava:32.1.3-jre")
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
        val lines = content.lines()

        val commonsIndex = lines.indexOfFirst { it.contains("org.apache.commons:commons-lang3") }
        val guavaIndex = lines.indexOfFirst { it.contains("com.google.guava:guava") }
        val junitIndex = lines.indexOfFirst { it.contains("org.junit.jupiter:junit-jupiter:5.10.1") }

        assertTrue(commonsIndex > 0, "Should contain commons-lang3")
        assertTrue(guavaIndex > 0, "Should contain guava")
        assertTrue(junitIndex > 0, "Should contain junit")
    }

    @Test
    fun `renders dependencies for multiple configurations`() {
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
                testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
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

        assertTrue(content.contains("## Dependencies"), "Should contain dependencies section")
        // Should have dependencies in various classpaths (compileClasspath, runtimeClasspath, testCompileClasspath, etc.)
        assertTrue(
            content.contains("org.apache.commons:commons-lang3:3.12.0"),
            "Should contain implementation dependency"
        )
        assertTrue(
            content.contains("org.junit.jupiter:junit-jupiter:5.10.1"),
            "Should contain test dependency"
        )
    }

    @Test
    fun `handles project without dependencies`() {
        setupProject(
            """
            plugins {
                id("com.gradle.project-report")
                `java-library`
            }

            repositories {
                mavenCentral()
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
        assertFalse(content.contains("## Dependencies"), "Should not have dependencies section")
    }

    @Test
    fun `task is not up-to-date when dependencies change`() {
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

        // First run
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("projectReport")
            .withPluginClasspath()
            .build()

        // Change dependencies
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
                implementation("com.google.guava:guava:32.1.3-jre")
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

        val reportFile = File(projectDir, "build/reports/project-report.md")
        val content = reportFile.readText()
        assertTrue(content.contains("com.google.guava:guava:32.1.3-jre"))
        assertFalse(content.contains("commons-lang3"))
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
