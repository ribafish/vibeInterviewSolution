package com.example

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

/**
 * A Gradle plugin that adds a task to generate a project report.
 */
class ProjectReportPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Create the extension
        val extension = project.extensions.create<ProjectReportExtension>("projectReport")

        // Register the task
        project.tasks.register<ProjectReportTask>("projectReport") {
            // Configure task properties from the extension
            projectName.set(project.name)
            projectGroup.set(project.group.toString())
            project.description?.let { projectDescription.set(it) }

            renderDependencies.set(extension.renderDependencies)

            // Set the default output file if not configured
            outputFile.convention(
                project.layout.buildDirectory.file("reports/project-report.md")
            )
            outputFile.set(extension.output)
        }
    }
}
