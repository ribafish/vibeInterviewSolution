package com.vibe.projectreport

import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
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
        assumeTrue(isGradleAvailable(gradleVersion), "Gradle $gradleVersion not available locally")

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

        assertTrue(result.report.contains("# metadata-only Project Report"))
        assertTrue(result.report.contains("- Group: com.example"))
        assertTrue(result.report.contains("- Description: Metadata only project"))
        assertTrue(result.report.contains("_No resolvable dependencies._"))
    }

    @ParameterizedTest
    @MethodSource("gradleVersions")
    fun `renders external dependency from local repo`(gradleVersion: String) {
        assumeTrue(isGradleAvailable(gradleVersion), "Gradle $gradleVersion not available locally")

        val repoDir = tempDir.resolve("repo-${gradleVersion.replace('.', '-') }").createDirectories()
        publishModule(repoDir, "com.example", "demo", "1.0.0")

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
                    add("resolvable", "com.example:demo:1.0.0")
                }
            """.trimIndent(),
        )

        assertTrue(result.report.contains("- resolvable"))
        assertTrue(result.report.contains("com.example:demo:1.0.0 - demo-1.0.0.jar"))
    }

    @ParameterizedTest
    @MethodSource("gradleVersions")
    fun `renders transitives in order`(gradleVersion: String) {
        assumeTrue(isGradleAvailable(gradleVersion), "Gradle $gradleVersion not available locally")

        val repoDir = tempDir.resolve("repo-transitive-${gradleVersion.replace('.', '-') }").createDirectories()
        publishModule(repoDir, "com.example", "leaf", "1.0.0")
        publishModule(
            repoDir,
            group = "com.example",
            name = "root",
            version = "1.0.0",
            dependencies = listOf(ModuleCoordinate("com.example", "leaf", "1.0.0")),
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
                    add("runtimeCopy", "com.example:root:1.0.0")
                }
            """.trimIndent(),
        )

        val rootIndex = result.report.indexOf("com.example:root:1.0.0 - root-1.0.0.jar")
        val leafIndex = result.report.indexOf("com.example:leaf:1.0.0 - leaf-1.0.0.jar")
        assertTrue(rootIndex > 0 && leafIndex > 0)
        assertTrue(leafIndex < rootIndex, "Transitive dependency should appear before root due to sorting")
    }

    @ParameterizedTest
    @MethodSource("gradleVersions")
    fun `renders project dependency`(gradleVersion: String) {
        assumeTrue(isGradleAvailable(gradleVersion), "Gradle $gradleVersion not available locally")

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

        assertTrue(result.report.contains("- projectDeps"))
        assertTrue(result.report.contains(":lib - lib.jar"))
    }

    @ParameterizedTest
    @MethodSource("gradleVersions")
    fun `respects renderDependencies flag`(gradleVersion: String) {
        assumeTrue(isGradleAvailable(gradleVersion), "Gradle $gradleVersion not available locally")

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

        assertTrue(!result.report.contains("Dependencies"))
    }

    @Test
    fun `writes custom output file`() {
        val gradleVersion = GradleVersion.current().version
        assumeTrue(isGradleAvailable(gradleVersion), "Gradle $gradleVersion not available locally")

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
        assertTrue(result.report.contains("# custom-output Project Report"))
    }

    @Test
    fun `supports configuration cache`() {
        val gradleVersion = GradleVersion.current().version
        assumeTrue(isGradleAvailable(gradleVersion), "Gradle $gradleVersion not available locally")

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

    private fun isGradleAvailable(version: String): Boolean {
        val userHome = System.getenv("GRADLE_USER_HOME")
            ?.let { Path.of(it) }
            ?: Path.of(System.getProperty("user.home")).resolve(".gradle")
        val distDir = userHome.resolve("wrapper").resolve("dists").resolve("gradle-$version-bin")
        return Files.exists(distDir) && Files.list(distDir).use { it.findAny().isPresent }
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
            listOf("8.14.3", "9.4.0", GradleVersion.current().version).distinct()
    }
}
