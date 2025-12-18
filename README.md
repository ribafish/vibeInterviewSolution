# Gradle Project Report Plugin (Claude Implementation)

A Gradle plugin that generates Markdown reports containing project metadata and dependency information. This implementation demonstrates compatibility with Gradle's performance features including build caching, configuration caching, and lazy configuration.

## Implementation Plan

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the detailed implementation strategy.

## Project Overview

This plugin provides:
- Markdown report generation with project metadata (name, group, description)
- Comprehensive dependency listing for all configurations
- Configurable output via extension DSL
- Full support for Gradle build cache and configuration cache
- Tested against Gradle 7.x, 8.x, and 9.x

## Usage

Apply the plugin in your `build.gradle.kts`:

```kotlin
plugins {
    id("com.gradle.project-report")
}

projectReport {
    renderDependencies.set(true) // default
    output.set(layout.buildDirectory.file("reports/project-report.md"))
}
```

Run the task:
```bash
./gradlew projectReport
```

## Technology Stack

- **Language**: Kotlin
- **Build**: Gradle Kotlin DSL
- **Testing**: Gradle TestKit + JUnit 5
- **CI/CD**: GitHub Actions

## Development

### Build the plugin
```bash
./gradlew build
```

### Run tests
```bash
./gradlew test
```

### Publish build scan
Build scans are automatically published to scans.gradle.com for every build.

---

This is part of a multi-solution project comparing different AI implementations. See other branches (codex, gemini) for alternative solutions.


