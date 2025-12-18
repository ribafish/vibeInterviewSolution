package com.gradle.projectreport

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ProjectReportTaskTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `task generates report with project metadata only`() {
        val project = createProject("test-project", "com.example", "Test description")
        val task = project.tasks.create("testReport", ProjectReportTask::class.java)

        val outputFile = File(tempDir, "report.md")
        task.projectName.set("test-project")
        task.projectGroup.set("com.example")
        task.projectDescription.set("Test description")
        task.renderDependencies.set(false)
        task.configurations.set(emptyList())
        task.outputFile.set(outputFile)

        task.generate()

        assertTrue(outputFile.exists(), "Report file should be created")
        val content = outputFile.readText()

        assertTrue(content.contains("# Project Report"), "Should contain title")
        assertTrue(content.contains("## Project Information"), "Should contain project info section")
        assertTrue(content.contains("**Name**: test-project"), "Should contain project name")
        assertTrue(content.contains("**Group**: com.example"), "Should contain project group")
        assertTrue(content.contains("**Description**: Test description"), "Should contain description")
        assertFalse(content.contains("## Dependencies"), "Should not contain dependencies section")
    }

    @Test
    fun `task generates report with dependencies`() {
        val project = createProject("test-project", "com.example", "Test description")
        val task = project.tasks.create("testReport", ProjectReportTask::class.java)

        val outputFile = File(tempDir, "report.md")
        val configurations = listOf(
            ConfigurationData(
                "compileClasspath",
                listOf(
                    "org.apache.commons:commons-lang3:3.12.0 - commons-lang3-3.12.0.jar",
                    "org.junit.jupiter:junit-jupiter:5.10.1 - junit-jupiter-5.10.1.jar"
                )
            )
        )

        task.projectName.set("test-project")
        task.projectGroup.set("com.example")
        task.projectDescription.set("Test description")
        task.renderDependencies.set(true)
        task.configurations.set(configurations)
        task.outputFile.set(outputFile)

        task.generate()

        assertTrue(outputFile.exists(), "Report file should be created")
        val content = outputFile.readText()

        assertTrue(content.contains("## Dependencies"), "Should contain dependencies section")
        assertTrue(content.contains("### compileClasspath"), "Should contain configuration name")
        assertTrue(
            content.contains("org.apache.commons:commons-lang3:3.12.0 - commons-lang3-3.12.0.jar"),
            "Should contain dependency"
        )
        assertTrue(
            content.contains("org.junit.jupiter:junit-jupiter:5.10.1 - junit-jupiter-5.10.1.jar"),
            "Should contain dependency"
        )
    }

    @Test
    fun `task sorts dependencies alphabetically`() {
        val project = createProject("test-project", "com.example", null)
        val task = project.tasks.create("testReport", ProjectReportTask::class.java)

        val outputFile = File(tempDir, "report.md")
        val configurations = listOf(
            ConfigurationData(
                "compileClasspath",
                listOf(
                    "org.junit.jupiter:junit-jupiter:5.10.1 - junit-jupiter-5.10.1.jar",
                    "org.apache.commons:commons-lang3:3.12.0 - commons-lang3-3.12.0.jar"
                )
            )
        )

        task.projectName.set("test-project")
        task.projectGroup.set("com.example")
        task.renderDependencies.set(true)
        task.configurations.set(configurations)
        task.outputFile.set(outputFile)

        task.generate()

        val content = outputFile.readText()
        val lines = content.lines()

        val lang3Index = lines.indexOfFirst { it.contains("commons-lang3") }
        val junitIndex = lines.indexOfFirst { it.contains("junit-jupiter") }

        assertTrue(lang3Index < junitIndex, "Dependencies should be sorted alphabetically")
    }

    @Test
    fun `task creates parent directories if they don't exist`() {
        val project = createProject("test-project", "com.example", null)
        val task = project.tasks.create("testReport", ProjectReportTask::class.java)

        val outputFile = File(tempDir, "nested/directory/report.md")
        task.projectName.set("test-project")
        task.projectGroup.set("com.example")
        task.renderDependencies.set(false)
        task.configurations.set(emptyList())
        task.outputFile.set(outputFile)

        task.generate()

        assertTrue(outputFile.exists(), "Report file should be created")
        assertTrue(outputFile.parentFile.exists(), "Parent directories should be created")
    }

    @Test
    fun `task handles empty dependencies list`() {
        val project = createProject("test-project", "com.example", null)
        val task = project.tasks.create("testReport", ProjectReportTask::class.java)

        val outputFile = File(tempDir, "report.md")
        task.projectName.set("test-project")
        task.projectGroup.set("com.example")
        task.renderDependencies.set(true)
        task.configurations.set(emptyList())
        task.outputFile.set(outputFile)

        task.generate()

        val content = outputFile.readText()
        assertFalse(content.contains("## Dependencies"), "Should not contain dependencies section when list is empty")
    }

    @Test
    fun `task handles optional properties`() {
        val project = createProject("test-project", null, null)
        val task = project.tasks.create("testReport", ProjectReportTask::class.java)

        val outputFile = File(tempDir, "report.md")
        task.projectName.set("test-project")
        task.renderDependencies.set(false)
        task.configurations.set(emptyList())
        task.outputFile.set(outputFile)

        task.generate()

        val content = outputFile.readText()
        assertTrue(content.contains("**Name**: test-project"), "Should contain project name")
        assertFalse(content.contains("**Group**"), "Should not contain group if not present")
        assertFalse(content.contains("**Description**"), "Should not contain description if not present")
    }

    private fun createProject(name: String, group: String?, description: String?): Project {
        val project = ProjectBuilder.builder().withName(name).build()
        if (group != null) project.group = group
        if (description != null) project.description = description
        return project
    }
}
