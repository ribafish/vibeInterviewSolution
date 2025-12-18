package com.example

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile

/**
 * Extension for configuring the ProjectReport plugin.
 */
abstract class ProjectReportExtension {

    /**
     * Controls whether dependency information should be rendered in the report.
     * Defaults to `true`.
     */
    @get:Input
    abstract val renderDependencies: Property<Boolean>

    /**
     * The output file for the generated report.
     * Defaults to `buildDir/reports/project-report.md`.
     *
     * This property is marked with `@OutputFile` as it represents the output of a task.
     */
    @get:OutputFile
    abstract val output: RegularFileProperty

    init {
        renderDependencies.convention(true)
    }
}
