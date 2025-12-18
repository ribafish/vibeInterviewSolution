# Gradle Project Report Plugin Implementation Plan

This document outlines the step-by-step plan for creating the Gradle project report plugin.

## Phase 1: Plugin Core Implementation

1.  **Project Setup (Gradle Multi-project/buildSrc):**
    *   Create the `buildSrc` directory.
    *   Configure `buildSrc/build.gradle.kts` to enable Kotlin DSL.
    *   Define the plugin ID: `com.example.project-report` in `buildSrc/src/main/resources/META-INF/gradle-plugins/com.example.project-report.properties`.
2.  **Extension Class:**
    *   Create `ProjectReportExtension.kt` to define `renderDependencies` (default true) and `output` (default `buildDir/reports/project-report.md`) properties, ensuring lazy configuration.
3.  **Task Class (`ProjectReportTask`):**
    *   Create `ProjectReportTask.kt` with input properties linked to the extension.
    *   Implement report generation: project metadata (name, group, description), and conditional dependency listing using `Configuration.incoming.resolutionResult.allDependencies` to format dependencies as `<group>:<artifactId>:<version> - <name of the jar file or folder>`.
4.  **Plugin Class:**
    *   Create `ProjectReportPlugin.kt` implementing `Plugin<Project>`.
    *   Register the `ProjectReport` task and initialize the `ProjectReportExtension`.

## Phase 2: Testing & Gradle Concepts

5.  **Basic Unit Test (Gradle TestKit):**
    *   Set up `buildSrc/src/test/kotlin` for TestKit and write a test to verify plugin application and basic report generation.
6.  **Property Testing:**
    *   Add tests for `renderDependencies` and `output` properties.
7.  **Lazy Configuration & Caching:**
    *   Ensure proper `@Input`, `@Output` annotations for build and configuration caching.

## Phase 3: Build & CI Integration

8.  **Root `build.gradle.kts`:**
    *   Apply the plugin and configure its extension.
    *   Include a test project for TestKit verification across Gradle versions.
9.  **`README.md`:**
    *   Create and document plugin usage, design decisions, and AI tool usage.
10. **GitHub Actions Workflow:**
    *   Create `.github/workflows/build.yaml` for CI, integrating `setup-gradle` and multi-version testing.

## Phase 4: Finalization

11. **Project Zip:**
    *   Provide instructions for creating `project.zip` excluding build artifacts.
