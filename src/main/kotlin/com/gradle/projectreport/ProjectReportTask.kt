package com.gradle.projectreport

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

@CacheableTask
abstract class ProjectReportTask : DefaultTask() {

    @get:Input
    abstract val rootProjectName: Property<String>

    @get:Input
    abstract val multiProject: Property<Boolean>

    @get:Input
    abstract val renderDependencies: Property<Boolean>

    @get:Nested
    abstract val projects: ListProperty<ProjectData>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        outputFile.asFile.get().apply {
            parentFile.mkdirs()
            writeText(buildReport())
        }
    }

    private fun buildReport() = buildString {
        if (multiProject.get()) {
            appendMultiProjectHeader()
        }
        projects.get().forEachIndexed { index, project ->
            appendProject(project)
            if (multiProject.get() && index < projects.get().size - 1) {
                appendLine("---")
                appendLine()
            }
        }
    }

    private fun StringBuilder.appendMultiProjectHeader() {
        val count = projects.get().size
        appendLine("# ${rootProjectName.get()}")
        appendLine()
        appendLine("Multi-project build report for **${rootProjectName.get()}** containing $count subproject(s).")
        appendLine()
    }

    private fun StringBuilder.appendProject(project: ProjectData) {
        val isMulti = multiProject.get()
        appendLine("${if (isMulti) "##" else "#"} ${project.name}")
        appendLine()
        appendProjectInfo(project, isMulti)
        if (renderDependencies.get() && project.configurations.isNotEmpty()) {
            appendDependencies(project, isMulti)
        }
    }

    private fun StringBuilder.appendProjectInfo(project: ProjectData, isMulti: Boolean) {
        appendLine("${if (isMulti) "###" else "##"} Project Information")
        appendLine()
        appendLine("- **Name**: ${project.name}")
        if (project.group.isNotBlank()) {
            appendLine("- **Group**: ${project.group}")
        }
        if (project.description.isNotBlank()) {
            appendLine("- **Description**: ${project.description}")
        }
        appendLine()
    }

    private fun StringBuilder.appendDependencies(project: ProjectData, isMulti: Boolean) {
        val headerPrefix = if (isMulti) "###" else "##"
        val configPrefix = if (isMulti) "####" else "###"

        appendLine("$headerPrefix Dependencies")
        appendLine()

        project.configurations
            .filter { it.dependencies.isNotEmpty() }
            .forEach { config ->
                appendLine("$configPrefix ${config.name}")
                appendLine()
                config.dependencies.sorted().forEach { dep ->
                    appendLine("- $dep")
                }
                appendLine()
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
