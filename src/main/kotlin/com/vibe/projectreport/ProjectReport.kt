package com.vibe.projectreport

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.file.RegularFileProperty
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
        val reportContent = buildReport(
            projectName.get(),
            projectGroup.get(),
            projectDescription.orNull,
            renderDependencies.get(),
            dependencyLines
        )

        val target = outputFile.get().asFile
        target.parentFile.mkdirs()
        target.writeText(reportContent)
    }
}

internal fun collectDependencies(project: Project): Map<String, List<String>> {
    return project.configurations
        .filter { it.isCanBeResolved }
        .associate { configuration ->
            val entries = linkedSetOf<String>()
            val artifacts = configuration.incoming.artifactView { view ->
                view.isLenient = true
            }.artifacts

            artifacts.forEach { artifact ->
                val coordinate = coordinateFor(artifact.id.componentIdentifier)
                val artifactName = artifact.file.name
                entries.add("$coordinate - $artifactName")
            }

            configuration.name to entries.sorted()
        }
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
    renderDependencies: Boolean,
    dependenciesByConfiguration: Map<String, List<String>>
): String {
    val builder = StringBuilder()
    builder.append("# ").append(name).append("\n\n")
    builder.append("## Metadata\n")
    builder.append("- Name: ").append(name).append("\n")
    builder.append("- Group: ").append(group).append("\n")
    builder.append("- Description: ").append(description?.takeIf { it.isNotBlank() } ?: "None").append("\n")

    if (renderDependencies && dependenciesByConfiguration.isNotEmpty()) {
        builder.append("\n## Dependencies\n")
        dependenciesByConfiguration
            .toSortedMap()
            .forEach { (configuration, lines) ->
                if (lines.isEmpty()) return@forEach
                builder.append("### ").append(configuration).append("\n")
                lines.forEach { line ->
                    builder.append("- ").append(line).append("\n")
                }
                builder.append("\n")
        }
    }

    return builder.toString()
}
