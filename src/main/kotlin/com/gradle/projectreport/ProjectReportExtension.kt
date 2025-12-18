package com.gradle.projectreport

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class ProjectReportExtension @Inject constructor(objects: ObjectFactory) {
    val renderDependencies: Property<Boolean> = objects.property(Boolean::class.java)
        .convention(true)

    val output: RegularFileProperty = objects.fileProperty()
        .convention(objects.fileProperty())
}
