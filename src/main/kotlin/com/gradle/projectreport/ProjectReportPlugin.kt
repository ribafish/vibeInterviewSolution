package com.gradle.projectreport

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

class ProjectReportPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("projectReport", ProjectReportExtension::class.java)
        extension.output.convention(project.layout.buildDirectory.file("reports/project-report.md"))

        project.tasks.register("projectReport", ProjectReportTask::class.java) {
            group = "documentation"
            description = "Generates a Markdown report with project metadata and dependencies"

            val targetProjects = if (project.subprojects.isEmpty()) listOf(project) else project.subprojects.toList()

            rootProjectName.set(project.name)
            multiProject.set(targetProjects.size > 1)
            renderDependencies.set(extension.renderDependencies)
            outputFile.set(extension.output)

            projects.set(project.provider {
                targetProjects.map { proj ->
                    ProjectData(
                        name = proj.name,
                        group = proj.group.toString(),
                        description = proj.description ?: "",
                        configurations = if (extension.renderDependencies.get()) {
                            collectDependencies(proj)
                        } else {
                            emptyList()
                        }
                    )
                }
            })
        }
    }

    private fun collectDependencies(project: Project): List<ConfigurationData> =
        project.configurations
            .filter { it.isCanBeResolved }
            .mapNotNull { config ->
                try {
                    resolveDependencies(config).takeIf { it.isNotEmpty() }
                        ?.let { ConfigurationData(config.name, it) }
                } catch (e: Exception) {
                    null
                }
            }

    private fun resolveDependencies(configuration: Configuration): List<String> {
        val result = mutableSetOf<String>()
        val visited = mutableSetOf<String>()

        try {
            configuration.incoming.resolutionResult.root.dependencies
                .filterIsInstance<org.gradle.api.artifacts.result.ResolvedDependencyResult>()
                .forEach { collectComponentDependencies(it.selected, result, visited, configuration) }
        } catch (e: Exception) {
            // Skip if resolution fails
        }

        return result.toList()
    }

    private fun collectComponentDependencies(
        component: org.gradle.api.artifacts.result.ResolvedComponentResult,
        result: MutableSet<String>,
        visited: MutableSet<String>,
        configuration: Configuration
    ) {
        val moduleVersion = component.moduleVersion ?: return
        val componentId = "${moduleVersion.group}:${moduleVersion.name}:${moduleVersion.version}"

        if (!visited.add(componentId)) return

        val artifactName = configuration.incoming.artifacts.artifacts
            .find { it.id.componentIdentifier.toString().contains(componentId) }
            ?.file?.name
            ?: "${moduleVersion.name}-${moduleVersion.version}.jar"

        result.add("$componentId - $artifactName")

        component.dependencies
            .filterIsInstance<org.gradle.api.artifacts.result.ResolvedDependencyResult>()
            .forEach { collectComponentDependencies(it.selected, result, visited, configuration) }
    }
}
