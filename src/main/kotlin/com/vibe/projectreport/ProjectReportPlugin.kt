package com.vibe.projectreport

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

class ProjectReportPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(
            "projectReport",
            ProjectReportExtension::class.java,
            project.objects,
            project.layout,
        )

        project.tasks.register("projectReport", ProjectReport::class.java) { task ->
            task.group = "Reporting"
            task.description = "Generates a Markdown report for the project."

            task.projectName.set(project.provider { project.name })
            task.projectGroup.set(
                project.provider {
                    project.group.toString().takeUnless { it == "unspecified" || it.isBlank() }
                },
            )
            task.projectDescription.set(project.provider { project.description })
            task.renderDependencies.set(extension.renderDependencies)
            task.outputFile.set(extension.outputFile)
            task.configurations.set(
                project.provider {
                    project.configurations.filter { it.isCanBeResolved }
                },
            )
        }
    }
}
