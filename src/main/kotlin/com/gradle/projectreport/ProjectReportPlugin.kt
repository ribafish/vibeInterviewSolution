package com.gradle.projectreport

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

class ProjectReportPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("projectReport", ProjectReportExtension::class.java)

        // Set default output location
        extension.output.convention(
            project.layout.buildDirectory.file("reports/project-report.md")
        )

        project.tasks.register("projectReport", ProjectReportTask::class.java).configure {
            group = "documentation"
            description = "Generates a Markdown report with project metadata and dependencies"

            // Wire basic project properties
            projectName.set(project.name)
            projectGroup.set(project.provider { project.group.toString() })
            projectDescription.set(project.provider {
                project.description ?: ""
            })

            // Wire extension properties
            renderDependencies.set(extension.renderDependencies)
            outputFile.set(extension.output)

            // Resolve configurations at configuration time
            configurations.set(project.provider {
                if (extension.renderDependencies.get()) {
                    collectDependencies(project)
                } else {
                    emptyList()
                }
            })
        }
    }

    private fun collectDependencies(project: Project): List<ConfigurationData> {
        return project.configurations
            .filter { it.isCanBeResolved }
            .mapNotNull { configuration ->
                try {
                    val dependencies = resolveDependencies(configuration)
                    if (dependencies.isNotEmpty()) {
                        ConfigurationData(configuration.name, dependencies)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    // Skip configurations that fail to resolve
                    null
                }
            }
    }

    private fun resolveDependencies(configuration: Configuration): List<String> {
        val result = mutableSetOf<String>()
        val visited = mutableSetOf<String>()

        try {
            val resolutionResult = configuration.incoming.resolutionResult
            val root = resolutionResult.root

            root.dependencies.forEach { dependency ->
                if (dependency is org.gradle.api.artifacts.result.ResolvedDependencyResult) {
                    collectComponentDependencies(dependency.selected, result, visited, configuration)
                }
            }
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
        val moduleVersion = component.moduleVersion
        if (moduleVersion == null) {
            return
        }

        val componentId = "${moduleVersion.group}:${moduleVersion.name}:${moduleVersion.version}"

        // Skip if already visited to prevent infinite recursion
        if (!visited.add(componentId)) {
            return
        }

        // Find the corresponding artifact
        val artifacts = configuration.incoming.artifacts.artifacts
        val matchingArtifact = artifacts.find { artifact ->
            val id = artifact.id.componentIdentifier
            id.toString().contains(componentId)
        }

        val artifactName = matchingArtifact?.file?.name ?: "${moduleVersion.name}-${moduleVersion.version}.jar"
        val dependency = "$componentId - $artifactName"
        result.add(dependency)

        component.dependencies.forEach { dep ->
            if (dep is org.gradle.api.artifacts.result.ResolvedDependencyResult) {
                collectComponentDependencies(dep.selected, result, visited, configuration)
            }
        }
    }
}
