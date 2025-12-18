package com.vibe.projectreport

import org.gradle.api.Plugin
import org.gradle.api.Project

class ProjectReportPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("projectReport", ProjectReportExtension::class.java)

        project.tasks.register("projectReport", ProjectReport::class.java) { task ->
            task.group = "Reporting"
            task.description = "Generates a Markdown report for the project."

            task.projectName.set(project.provider { project.name })
            task.projectGroup.set(project.provider { project.group.toString() })
            task.projectDescription.set(project.provider { project.description ?: "" })

            task.renderDependencies.set(extension.renderDependencies)
            task.outputFile.set(extension.outputFile)

            task.dependenciesListing.set(
                extension.renderDependencies.flatMap { render ->
                    if (render) {
                        project.provider { collectDependencies(project) }
                    } else {
                        project.provider { emptyList() }
                    }
                }
            )
        }
    }
}
