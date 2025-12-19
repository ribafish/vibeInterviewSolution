# Gradle Project Report Plugin

This project implements a Gradle plugin that generates a Markdown report containing essential project metadata and its dependencies. This plugin is designed to showcase engineering abilities in Gradle plugin development, adhering to modern Gradle practices.

## Implementation Plan

The detailed implementation plan for this project can be found in [PLAN.md](PLAN.md).

## Project Overview

The `project-report` Gradle plugin registers a task named `projectReport` that, when executed, produces a Markdown file. This report includes:
*   The project's name, group, and description.
*   A flat list of all resolved dependencies for each configuration, formatted as `<group>:<artifactId>:<version> - <name of the jar file or folder>`.

The plugin is configurable via an extension, allowing users to control whether dependencies are rendered and specify the output file location.

## Usage

### Applying the Plugin

To apply the plugin, include `buildSrc` in your `settings.gradle.kts` (no explicit `includeBuild` is needed as Gradle automatically recognizes `buildSrc` as a build source) and then apply the plugin ID in your project's `build.gradle.kts`:

```kotlin
// settings.gradle.kts
rootProject.name = "your-project-name"

// build.gradle.kts
plugins {
    id("com.example.project-report")
    // Apply other necessary plugins, e.g., 'java-library' for dependency management
    id("java-library")
}

// Optional: Configure the project metadata
group = "com.yourcompany"
version = "1.0.0"
description = "Description of your project"
```

### Configuring the Plugin

The plugin can be configured using the `projectReport` extension in your `build.gradle.kts`:

```kotlin
projectReport {
    // Optional: Set to false to exclude the dependencies section from the report. Default is true.
    renderDependencies = false 

    // Optional: Specify a custom output file path for the report.
    // Default is layout.buildDirectory.file("reports/project-report.md")
    output = layout.buildDirectory.file("custom-reports/my-special-report.md")
}
```

### Running the Report Task

To generate the report, execute the `projectReport` task:

```bash
./gradlew projectReport
```

The report will be generated at `build/reports/project-report.md` by default, or at the path specified in the `output` configuration.

## Design Decisions

*   **Kotlin DSL**: The plugin is developed entirely using Kotlin DSL for conciseness, type-safety, and a more modern Gradle experience.
*   **Lazy Configuration**: All configurable properties (`renderDependencies`, `output`) are implemented using Gradle's `Property` and `RegularFileProperty` types. This ensures lazy evaluation, improving build performance and compatibility with Gradle's configuration caching.
*   **Build & Configuration Caching**: By utilizing lazy properties and marking task inputs/outputs with appropriate annotations (`@Input`, `@OutputFile`), the plugin is designed to be compatible with Gradle's build and configuration caching mechanisms, leading to faster incremental builds.
*   **Gradle TestKit**: The plugin includes automated tests written with Gradle TestKit, ensuring its functionality across different Gradle versions and configurations. This allows for reliable testing of plugin application, task registration, and report content generation.
*   **Dependency Resolution**: The plugin uses `Configuration.incoming.resolutionResult.allDependencies` to resolve and list dependencies, providing a comprehensive view of the project's dependency graph.

## AI Tooling

This project was developed with the assistance of an AI assistant (Gemini). The AI was used for:
*   Interpreting project requirements from the provided PDF document.
*   Generating initial code structures for the Gradle plugin classes (`ProjectReportExtension`, `ProjectReportTask`, `ProjectReportPlugin`).
*   Implementing the core logic for report generation and dependency resolution.
*   Setting up the testing infrastructure using Gradle TestKit and writing initial test cases.
*   Debugging and resolving compilation and runtime errors encountered during development.
*   Creating and updating build scripts (`build.gradle.kts`, `settings.gradle.kts`).
*   Documenting the project in `PLAN.md` and `README.md`.

The AI was instructed to follow modern Gradle best practices, including Kotlin DSL, lazy configuration, and robust testing. All critical changes and decisions were reviewed and guided by the human developer (me).

## Creating project.zip

To create a `project.zip` file for submission, which includes all necessary source code and project files but excludes build artifacts and local development files, run the following command from the root directory of the project:

```bash
zip -r project.zip . -x "*/build/*" -x "*/.gradle/*" -x ".git/*" -x "*.DS_Store"
```

This command will create a `project.zip` file in the project's root directory. It includes:
- The complete `buildSrc` directory with the plugin source code and tests.
- Gradle wrapper scripts (`gradlew`, `gradlew.bat`).
- The `.github/workflows/build.yaml` file for CI.
- All `build.gradle.kts` and `settings.gradle.kts` files.
- `PLAN.md` and `README.md`.

It excludes:
- All `build` directories.
- The `.gradle` directory, which contains caches and build-related files.
- The `.git` directory.
- System-specific files like `.DS_Store`.