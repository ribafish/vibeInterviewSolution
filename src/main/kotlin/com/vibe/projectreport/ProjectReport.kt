package com.vibe.projectreport

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class ProjectReport : DefaultTask() {
    @get:Input
    abstract val projectName: Property<String>

    @get:Input
    @get:Optional
    abstract val projectGroup: Property<String?>

    @get:Input
    @get:Optional
    abstract val projectDescription: Property<String?>

    @get:Input
    abstract val renderDependencies: Property<Boolean>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Internal
    abstract val configurations: ListProperty<Configuration>

    @TaskAction
    fun generate() {
        val output = outputFile.get().asFile
        output.parentFile.mkdirs()
        output.writeText(buildReport())
    }

    private fun buildReport(): String {
        val builder = StringBuilder()
        builder.append("# ").append(projectName.get()).append(" Project Report\n\n")
        builder.append("## Project Metadata\n")
        builder.append("- Name: ").append(projectName.get()).append("\n")
        builder.append("- Group: ").append(projectGroup.orNull ?: "(none)").append("\n")
        builder.append("- Description: ").append(projectDescription.orNull ?: "(none)").append("\n")

        if (renderDependencies.get()) {
            builder.append("\n## Dependencies\n")
            val dependencies = collectDependencies()
            if (dependencies.isEmpty()) {
                builder.append("_No resolvable dependencies._\n")
            } else {
                var currentConfig: String? = null
                dependencies.forEach { entry ->
                    if (entry.configuration != currentConfig) {
                        currentConfig = entry.configuration
                        builder.append("\n- ").append(entry.configuration).append("\n")
                    }
                    builder.append("  - ")
                        .append(entry.notation)
                        .append(" - ")
                        .append(entry.artifactFileName)
                        .append("\n")
                }
            }
        }

        return builder.toString()
    }

    private fun collectDependencies(): List<DependencyEntry> {
        val entries = mutableListOf<DependencyEntry>()
        configurations.get()
            .filter { it.isCanBeResolved }
            .sortedBy { it.name }
            .forEach { configuration ->
                val artifacts = configuration.incoming.artifactView { viewConfiguration ->
                    viewConfiguration.isLenient = true
                }.artifacts

                artifacts.artifacts.forEach { artifact ->
                    val identifier = artifact.id.componentIdentifier
                    val notation = coordinateFor(identifier)
                    val artifactName = artifact.file.name
                    entries.add(
                        DependencyEntry(
                            configuration = configuration.name,
                            notation = notation,
                            artifactFileName = artifactName,
                        ),
                    )
                }
            }

        return entries.sortedWith(compareBy({ it.configuration }, { it.notation }))
    }

    private fun coordinateFor(id: ComponentIdentifier): String =
        when (id) {
            is ModuleComponentIdentifier -> "${id.group}:${id.module}:${id.version}"
            is ProjectComponentIdentifier -> id.projectPath
            else -> id.displayName
        }

    private data class DependencyEntry(
        val configuration: String,
        val notation: String,
        val artifactFileName: String,
    )
}
