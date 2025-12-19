package com.vibe.projectreport

import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class ProjectReportPluginFunctionalTest {

    @TempDir
    lateinit var tempDir: Path

    @ParameterizedTest
    @MethodSource("gradleVersions")
    fun `generates metadata only report`(gradleVersion: String) {
        val result = runBuild(
            gradleVersion = gradleVersion,
            projectName = "metadata-only",
            buildFileContent = """
                plugins {
                    id("com.vibe.projectreport")
                }
                group = "com.example"
                description = "Metadata only project"
            """.trimIndent(),
        )

        assertTrue(result.report.contains("# metadata-only"))
        assertTrue(result.report.contains("- Group: com.example"))
        assertTrue(result.report.contains("- Description: Metadata only project"))
        assertTrue(!result.report.contains("## Dependencies"))
    }

    @ParameterizedTest
    @MethodSource("gradleVersions")
    fun `renders external dependency from local repo`(gradleVersion: String) {
        val repoDir = tempDir.resolve("repo-${gradleVersion.replace('.', '-') }").createDirectories()
        publishModule(repoDir, "org.apache.commons", "commons-lang3", "3.12.0")

        val result = runBuild(
            gradleVersion = gradleVersion,
            projectName = "external-dep",
            buildFileContent = """
                plugins {
                    id("com.vibe.projectreport")
                }
                repositories {
                    maven { url = uri("${repoDir.toUri()}") }
                }
                configurations {
                    create("resolvable") {
                        isCanBeResolved = true
                        isCanBeConsumed = false
                    }
                }
                dependencies {
                    add("resolvable", "org.apache.commons:commons-lang3:3.12.0")
                }
            """.trimIndent(),
        )

        assertTrue(result.report.contains("## Dependencies"))
        assertTrue(result.report.contains("- org.apache.commons:commons-lang3:3.12.0 - commons-lang3-3.12.0.jar"))
    }

    @ParameterizedTest
    @MethodSource("gradleVersions")
    fun `renders transitives in order`(gradleVersion: String) {
        val repoDir = tempDir.resolve("repo-transitive-${gradleVersion.replace('.', '-') }").createDirectories()
        publishModule(repoDir, "org.apache.commons", "commons-lang3", "3.12.0")
        publishModule(
            repoDir,
            group = "org.apache.commons",
            name = "commons-text",
            version = "1.10.0",
            dependencies = listOf(ModuleCoordinate("org.apache.commons", "commons-lang3", "3.12.0")),
        )

        val result = runBuild(
            gradleVersion = gradleVersion,
            projectName = "transitive",
            buildFileContent = """
                plugins {
                    id("com.vibe.projectreport")
                }
                repositories {
                    maven { url = uri("${repoDir.toUri()}") }
                }
                configurations {
                    create("runtimeCopy") {
                        isCanBeResolved = true
                        isCanBeConsumed = false
                    }
                }
                dependencies {
                    add("runtimeCopy", "org.apache.commons:commons-text:1.10.0")
                }
            """.trimIndent(),
        )

        val rootIndex = result.report.indexOf("org.apache.commons:commons-text:1.10.0 - commons-text-1.10.0.jar")
        val leafIndex = result.report.indexOf("org.apache.commons:commons-lang3:3.12.0 - commons-lang3-3.12.0.jar")
        assertTrue(rootIndex > 0 && leafIndex > 0)
        assertTrue(leafIndex < rootIndex, "Transitive dependency should appear before root due to sorting")
    }

    @ParameterizedTest
    @MethodSource("gradleVersions")
    fun `renders project dependency`(gradleVersion: String) {
        val projectDir = tempDir.resolve("project-dep-$gradleVersion").createDirectories()
        projectDir.resolve("settings.gradle.kts").writeText(
            """
            rootProject.name = "project-dependency"
            include("lib")
            """.trimIndent(),
        )
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("com.vibe.projectreport")
            }
            configurations {
                create("projectDeps") {
                    isCanBeResolved = true
                    isCanBeConsumed = false
                }
            }
            dependencies {
                add("projectDeps", project(":lib"))
            }
            """.trimIndent(),
        )
        val libDir = projectDir.resolve("lib").createDirectories()
        libDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                java
            }
            """.trimIndent(),
        )

        val result = runBuild(
            gradleVersion = gradleVersion,
            projectName = "project-dependency",
            buildFileContent = Files.readString(projectDir.resolve("build.gradle.kts")),
            settingsContent = Files.readString(projectDir.resolve("settings.gradle.kts")),
            projectDirOverride = projectDir,
        )

        assertTrue(result.report.contains("- :lib - lib.jar"))
    }

    @ParameterizedTest
    @MethodSource("gradleVersions")
    fun `respects renderDependencies flag`(gradleVersion: String) {
        val result = runBuild(
            gradleVersion = gradleVersion,
            projectName = "skip-deps",
            buildFileContent = """
                plugins {
                    id("com.vibe.projectreport")
                }
                projectReport {
                    renderDependencies.set(false)
                }
            """.trimIndent(),
        )

        assertTrue(!result.report.contains("## Dependencies"))
    }

    @Test
    fun `writes custom output file`() {
        val gradleVersion = GradleVersion.current().version

        val result = runBuild(
            gradleVersion = gradleVersion,
            projectName = "custom-output",
            buildFileContent = """
                plugins {
                    id("com.vibe.projectreport")
                }
                projectReport {
                    outputFile.set(layout.buildDirectory.file("reports/custom/report.md"))
                }
            """.trimIndent(),
            expectedOutput = "build/reports/custom/report.md",
        )

        assertTrue(result.reportPath.endsWith("reports/custom/report.md"))
        assertTrue(result.report.contains("# custom-output"))
    }

    @Test
    fun `supports configuration cache`() {
        val gradleVersion = GradleVersion.current().version

        val projectDir = tempDir.resolve("config-cache").createDirectories()
        projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "config-cache"""")
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("com.vibe.projectreport")
            }
            """.trimIndent(),
        )

        val firstRun = runBuild(
            gradleVersion = gradleVersion,
            projectName = "config-cache",
            buildFileContent = Files.readString(projectDir.resolve("build.gradle.kts")),
            settingsContent = Files.readString(projectDir.resolve("settings.gradle.kts")),
            projectDirOverride = projectDir,
            additionalArgs = listOf("--configuration-cache"),
        )
        assertTrue(firstRun.output.contains("Configuration cache entry stored"))

        val secondRun = runBuild(
            gradleVersion = gradleVersion,
            projectName = "config-cache",
            buildFileContent = Files.readString(projectDir.resolve("build.gradle.kts")),
            settingsContent = Files.readString(projectDir.resolve("settings.gradle.kts")),
            projectDirOverride = projectDir,
            additionalArgs = listOf("--configuration-cache"),
        )
        assertTrue(secondRun.output.contains("Reusing configuration cache"))
    }

    private fun runBuild(
        gradleVersion: String,
        projectName: String,
        buildFileContent: String,
        settingsContent: String? = null,
        expectedOutput: String = "build/reports/project-report.md",
        additionalArgs: List<String> = emptyList(),
        projectDirOverride: Path? = null,
    ): ReportResult {
        val projectDir = projectDirOverride ?: tempDir.resolve("${projectName}-${gradleVersion.replace('.', '-')}")
        projectDir.createDirectories()
        val settings = settingsContent ?: """rootProject.name = "$projectName""""
        projectDir.resolve("settings.gradle.kts").writeText(settings)
        projectDir.resolve("build.gradle.kts").writeText(buildFileContent)

        val runner = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(projectDir.toFile())
            .withGradleVersion(gradleVersion)
            .withArguments(listOf("projectReport", "--stacktrace", "--warning-mode=fail") + additionalArgs)

        val result = runner.build()
        val reportFile = projectDir.resolve(expectedOutput)
        val reportText = Files.readString(reportFile)
        return ReportResult(
            projectDir = projectDir,
            reportPath = reportFile.toString(),
            report = reportText,
            output = result.output,
        )
    }

    private fun publishModule(
        repoDir: Path,
        group: String,
        name: String,
        version: String,
        dependencies: List<ModuleCoordinate> = emptyList(),
    ) {
        val moduleDir = repoDir.resolve(group.replace('.', File.separatorChar))
            .resolve(name)
            .resolve(version)
        moduleDir.createDirectories()

        val pom = buildString {
            appendLine("""<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">""")
            appendLine("<modelVersion>4.0.0</modelVersion>")
            appendLine("<groupId>$group</groupId>")
            appendLine("<artifactId>$name</artifactId>")
            appendLine("<version>$version</version>")
            if (dependencies.isNotEmpty()) {
                appendLine("<dependencies>")
                dependencies.forEach { dependency ->
                    appendLine("<dependency>")
                    appendLine("<groupId>${dependency.group}</groupId>")
                    appendLine("<artifactId>${dependency.name}</artifactId>")
                    appendLine("<version>${dependency.version}</version>")
                    appendLine("</dependency>")
                }
                appendLine("</dependencies>")
            }
            appendLine("</project>")
        }

        moduleDir.resolve("$name-$version.pom").writeText(pom)
        createJar(moduleDir.resolve("$name-$version.jar"))
    }

    private fun createJar(target: Path) {
        JarOutputStream(target.toFile().outputStream()).use { jar ->
            jar.putNextEntry(ZipEntry("placeholder.txt"))
            jar.write("content".toByteArray())
            jar.closeEntry()
        }
    }

    private data class ModuleCoordinate(val group: String, val name: String, val version: String)

    private data class ReportResult(
        val projectDir: Path,
        val reportPath: String,
        val report: String,
        val output: String,
    )

    companion object {
        @JvmStatic
        fun gradleVersions(): List<String> =
            listOf("7.6.4", "8.14.3", "9.2.1").distinct()
    }
}
