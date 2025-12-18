package com.example

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Optional
import java.io.File
import java.io.FileWriter

/**
 * A Gradle task that generates a report containing project metadata and, optionally, its dependencies.
 */
abstract class ProjectReportTask : DefaultTask() {

    @get:Input
    abstract val projectName: Property<String>

    @get:Input
    abstract val projectGroup: Property<String>

    @get:Input
    @get:Optional
    abstract val projectDescription: Property<String>

    @get:Input
    abstract val renderDependencies: Property<Boolean>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generateReport() {
        val reportFile = outputFile.asFile.get()
        reportFile.parentFile.mkdirs()

        FileWriter(reportFile).use { writer ->
            writer.write("# Project: ${projectName.get()}\n\n")
            writer.write("**Group:** ${projectGroup.get()}\n")
            projectDescription.orNull?.let {
                writer.write("**Description:** $it\n")
            }
            writer.write("\n")

            if (renderDependencies.get()) {
                writer.write("## Configurations and Dependencies\n\n")
                project.configurations.forEach { configuration ->
                    // Only process configurations that can be resolved
                    if (configuration.isCanBeResolved) {
                        writer.write("### Configuration: ${configuration.name}\n\n")
                        val resolvedDependencies = try {
                            configuration.incoming.resolutionResult.allDependencies
                                .filterIsInstance<org.gradle.api.artifacts.ResolvedDependency>()
                        } catch (e: Exception) {
                            // Log a warning if resolution fails for a configuration
                            logger.warn("Could not resolve dependencies for configuration '${configuration.name}': ${e.message}")
                            emptySet()
                        }

                        if (resolvedDependencies.isNotEmpty()) {
                            resolvedDependencies.forEach { dependency ->
                                val moduleIdentifier = dependency.module.id
                                val dependencyNotation = "${moduleIdentifier.group}:${moduleIdentifier.name}:${moduleIdentifier.version}"

                                // Attempt to find the artifact file. This might be tricky as ResolvedDependency
                                // doesn't directly expose the artifact file path easily.
                                // For a simple case, we might assume the name from the module.
                                val artifactFileName = "${moduleIdentifier.name}-${moduleIdentifier.version}.jar" // Simplified assumption

                                writer.write("- $dependencyNotation - $artifactFileName\n")

                                // Recursively add children dependencies
                                dependency.children.forEach { child ->
                                    val childModuleIdentifier = child.module.id
                                    val childDependencyNotation = "${childModuleIdentifier.group}:${childModuleIdentifier.name}:${childModuleIdentifier.version}"
                                    val childArtifactFileName = "${childModuleIdentifier.name}-${childModuleIdentifier.version}.jar" // Simplified assumption
                                    writer.write("  - $childDependencyNotation - $childArtifactFileName\n")
                                }
                            }
                        } else {
                            writer.write("  No dependencies.\n")
                        }
                        writer.write("\n")
                    }
                }
            }
        }
        logger.lifecycle("Project report generated at: ${reportFile.absolutePath}")
    }
}
