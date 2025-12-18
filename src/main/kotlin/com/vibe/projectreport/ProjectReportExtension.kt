package com.vibe.projectreport

import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

open class ProjectReportExtension @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout,
) {
    val renderDependencies: Property<Boolean> = objects.property(Boolean::class.java)
    val outputFile: RegularFileProperty = objects.fileProperty()

    init {
        renderDependencies.convention(true)
        outputFile.convention(layout.buildDirectory.file("reports/project-report.md"))
    }
}
