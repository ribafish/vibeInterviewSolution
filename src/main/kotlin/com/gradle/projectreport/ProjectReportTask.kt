package com.gradle.projectreport

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
abstract class ProjectReportTask : DefaultTask() {

    @get:Input
    abstract val projectName: Property<String>

    @get:Input
    @get:Optional
    abstract val projectGroup: Property<String>

    @get:Input
    @get:Optional
    abstract val projectDescription: Property<String>

    @get:Input
    abstract val renderDependencies: Property<Boolean>

    @get:Nested
    abstract val configurations: ListProperty<ConfigurationData>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val output = outputFile.asFile.get()
        output.parentFile.mkdirs()

        val content = buildString {
            appendLine("# Project Report")
            appendLine()
            appendLine("## Project Information")
            appendLine()
            appendLine("- **Name**: ${projectName.get()}")
            if (projectGroup.isPresent) {
                appendLine("- **Group**: ${projectGroup.get()}")
            }
            if (projectDescription.isPresent) {
                appendLine("- **Description**: ${projectDescription.get()}")
            }
            appendLine()

            if (renderDependencies.get() && configurations.get().isNotEmpty()) {
                appendLine("## Dependencies")
                appendLine()

                configurations.get().forEach { configData ->
                    if (configData.dependencies.isNotEmpty()) {
                        appendLine("### ${configData.name}")
                        appendLine()
                        configData.dependencies.sorted().forEach { dep ->
                            appendLine("- $dep")
                        }
                        appendLine()
                    }
                }
            }
        }

        output.writeText(content)
    }
}

data class ConfigurationData(
    @get:Input
    val name: String,

    @get:Input
    val dependencies: List<String>
) : java.io.Serializable
