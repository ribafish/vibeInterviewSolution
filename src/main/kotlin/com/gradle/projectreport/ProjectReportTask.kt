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

    @get:Nested
    abstract val subprojects: ListProperty<ProjectData>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val output = outputFile.asFile.get()
        output.parentFile.mkdirs()

        val subprojectsList = subprojects.get()
        val hasSubprojects = subprojectsList.isNotEmpty()

        val content = buildString {
            if (hasSubprojects) {
                // Multi-project report
                appendLine("# ${projectName.get()}")
                appendLine()
                appendLine("Multi-project build report for **${projectName.get()}** containing ${subprojectsList.size} subproject(s).")
                appendLine()

                subprojectsList.forEach { projectData ->
                    renderProject(projectData)
                    appendLine("---")
                    appendLine()
                }
            } else {
                // Single project report
                val projectData = ProjectData(
                    name = projectName.get(),
                    group = if (projectGroup.isPresent) projectGroup.get() else "",
                    description = if (projectDescription.isPresent) projectDescription.get() else "",
                    configurations = configurations.get()
                )
                renderProject(projectData)
            }
        }

        output.writeText(content)
    }

    private fun StringBuilder.renderProject(projectData: ProjectData) {
        appendLine("## ${projectData.name}")
        appendLine()
        appendLine("### Project Information")
        appendLine()
        appendLine("- **Name**: ${projectData.name}")
        if (projectData.group.isNotBlank()) {
            appendLine("- **Group**: ${projectData.group}")
        }
        if (projectData.description.isNotBlank()) {
            appendLine("- **Description**: ${projectData.description}")
        }
        appendLine()

        if (renderDependencies.get() && projectData.configurations.isNotEmpty()) {
            appendLine("### Dependencies")
            appendLine()

            projectData.configurations.forEach { configData ->
                if (configData.dependencies.isNotEmpty()) {
                    appendLine("#### ${configData.name}")
                    appendLine()
                    configData.dependencies.sorted().forEach { dep ->
                        appendLine("- $dep")
                    }
                    appendLine()
                }
            }
        }
    }
}

data class ConfigurationData(
    @get:Input
    val name: String,

    @get:Input
    val dependencies: List<String>
) : java.io.Serializable

data class ProjectData(
    @get:Input
    val name: String,

    @get:Input
    val group: String,

    @get:Input
    val description: String,

    @get:Nested
    val configurations: List<ConfigurationData>
) : java.io.Serializable
