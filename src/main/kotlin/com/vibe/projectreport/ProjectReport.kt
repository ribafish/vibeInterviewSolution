package com.vibe.projectreport

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class ProjectReport : DefaultTask() {
    @get:Input
    abstract val projectName: Property<String>

    @get:Input
    abstract val projectGroup: Property<String>

    @get:Input
    @get:Optional
    abstract val projectDescription: Property<String>

    @get:Input
    abstract val renderDependencies: Property<Boolean>

    @get:Input
    abstract val dependenciesByConfiguration: MapProperty<String, List<String>>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val dependencyLines = if (renderDependencies.get()) dependenciesByConfiguration.get() else emptyMap()
        val reportContent = buildReport(projectName.get(), projectGroup.get(), projectDescription.orNull, dependencyLines)

        val target = outputFile.get().asFile
        target.parentFile.mkdirs()
        target.writeText(reportContent)
    }
}

internal fun collectDependencies(project: Project): Map<String, List<String>> =
    project.configurations
        .asSequence()
        .filter { it.isCanBeResolved }
        .associate { configuration ->
            val entries = linkedSetOf<String>()
            configuration.incoming
                .artifactView { view -> view.isLenient = true }
                .artifacts
                .forEach { artifact ->
                    val coordinate = coordinateFor(artifact.id.componentIdentifier)
                    entries += "$coordinate - ${artifact.file.name}"
                }
            configuration.name to entries.sorted()
        }

private fun coordinateFor(identifier: ComponentIdentifier): String = when (identifier) {
    is ModuleComponentIdentifier -> "${identifier.group}:${identifier.module}:${identifier.version}"
    is ProjectComponentIdentifier -> identifier.projectPath
    else -> identifier.displayName
}

internal fun buildReport(
    name: String,
    group: String,
    description: String?,
    dependenciesByConfiguration: Map<String, List<String>>
): String {
    return buildString {
        appendLine("# $name")
        appendLine()
        appendLine("## Metadata")
        appendLine("- Name: $name")
        appendLine("- Group: $group")
        appendLine("- Description: ${description?.takeIf { it.isNotBlank() } ?: "None"}")

        if (dependenciesByConfiguration.isNotEmpty()) {
            appendLine()
            appendLine("## Dependencies")
            dependenciesByConfiguration
                .toSortedMap()
                .filterValues { it.isNotEmpty() }
                .forEach { (configuration, lines) ->
                    appendLine("### $configuration")
                    lines.forEach { line -> appendLine("- $line") }
                    appendLine()
                }
        }
    }
}
