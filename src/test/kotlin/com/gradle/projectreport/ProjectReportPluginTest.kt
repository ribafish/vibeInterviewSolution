package com.gradle.projectreport

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ProjectReportPluginTest {

    @Test
    fun `plugin registers extension`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.gradle.project-report")

        val extension = project.extensions.findByType(ProjectReportExtension::class.java)
        assertNotNull(extension, "Extension should be registered")
    }

    @Test
    fun `plugin registers task`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.gradle.project-report")

        val task = project.tasks.findByName("projectReport")
        assertNotNull(task, "Task should be registered")
        assertTrue(task is ProjectReportTask, "Task should be of type ProjectReportTask")
    }

    @Test
    fun `extension has default values`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.gradle.project-report")

        val extension = project.extensions.getByType(ProjectReportExtension::class.java)

        assertTrue(extension.renderDependencies.get(), "renderDependencies should default to true")
        assertTrue(
            extension.output.get().asFile.path.contains("build/reports/project-report.md"),
            "output should default to build/reports/project-report.md"
        )
    }

    @Test
    fun `task has correct properties`() {
        val project = ProjectBuilder.builder().withName("test-project").build()
        project.group = "com.example"
        project.description = "Test project description"

        project.pluginManager.apply("com.gradle.project-report")

        val task = project.tasks.getByName("projectReport") as ProjectReportTask

        assertEquals("documentation", task.group, "Task should be in documentation group")
        assertEquals(
            "Generates a Markdown report with project metadata and dependencies",
            task.description,
            "Task should have proper description"
        )
        assertEquals("test-project", task.projectName.get(), "Task should have project name")
        assertEquals("com.example", task.projectGroup.get(), "Task should have project group")
        assertEquals("Test project description", task.projectDescription.get(), "Task should have project description")
    }

    @Test
    fun `extension configuration is wired to task`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.gradle.project-report")

        val extension = project.extensions.getByType(ProjectReportExtension::class.java)
        extension.renderDependencies.set(false)

        val task = project.tasks.getByName("projectReport") as ProjectReportTask

        assertFalse(task.renderDependencies.get(), "Task should reflect extension configuration")
    }
}
