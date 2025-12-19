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
import java.io.PrintWriter

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

        FileWriter(reportFile).use { fileWriter ->
            PrintWriter(fileWriter).use { printWriter ->
                printWriter.write("# Project: ${projectName.get()}\n\n")
                printWriter.write("**Group:** ${projectGroup.get()}\n")
                projectDescription.orNull?.let {
                    printWriter.write("**Description:** $it\n")
                }
                printWriter.write("\n")


            }
        }
        logger.lifecycle("Project report generated at: ${reportFile.absolutePath}")
    }
}
