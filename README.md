# Gradle Project Report Plugin

A Gradle plugin that generates Markdown reports containing project metadata and dependency information. This implementation demonstrates compatibility with Gradle's performance features including build caching, configuration caching, and lazy configuration.

## Implementation Plan

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the detailed implementation strategy.

## Project Overview

This plugin provides:
- Markdown report generation with project metadata (name, group, description)
- Comprehensive dependency listing for all configurations
- **Multi-project support**: Consolidated reports for all subprojects when applied to root project
- Configurable output via extension DSL
- Full support for Gradle build cache and configuration cache
- Tested against Gradle 7.x, 8.x, and 9.x

## Usage

### Single Project

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

### Multi-Project Build

Apply the plugin to the **root** `build.gradle.kts` to generate a consolidated report:

```kotlin
plugins {
    id("com.gradle.project-report")
}

projectReport {
    renderDependencies.set(true)
    output.set(layout.buildDirectory.file("reports/multi-project-report.md"))
}
```

This automatically includes all subprojects in a single consolidated report, showing:
- All subprojects with their metadata
- Complete dependency information for each subproject
- Project-to-project dependencies
- All transitive dependencies

See the [example/](example/) directory for a complete working multi-project demonstration.

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

## Design Decisions and Alternatives

### Language Selection: Kotlin
Kotlin was chosen for plugin implementation over Groovy and Java due to:
- Superior type safety and null safety features
- Excellent IDE support and autocompletion
- First-class Gradle API support with property delegates
- More concise and readable code for plugin development

### Dependency Resolution Approach
The plugin uses `Configuration.incoming.resolutionResult` API to process the dependency graph, which:
- Provides access to the complete resolved dependency tree including transitives
- Allows matching artifacts to their component identifiers
- Handles configuration resolution errors gracefully
- Works efficiently with Gradle's lazy configuration model

### Caching Strategy
Build caching is implemented using:
- `@CacheableTask` annotation on the task class
- Proper input annotations (`@Input`, `@Nested`) for all task inputs
- `@OutputFile` annotation for the report file
- Deterministic output generation (sorted dependencies)

Configuration caching compatibility achieved by:
- Avoiding direct `Project` access in task actions
- Using `Property<T>` types for all configuration
- Resolving dependencies at configuration time, not execution time
- Storing only serializable data in task inputs

### Testing Strategy
Multi-layered testing approach:
- Unit tests: Direct task testing with ProjectBuilder
- Functional tests: Full integration tests with TestKit
- Multi-version compatibility tests: Parameterized tests across Gradle 7.x, 8.x, 9.x
- Cache verification tests: Ensuring FROM_CACHE and UP_TO_DATE outcomes work correctly

### Alternatives Considered
**Alternative 1: Runtime Dependency Resolution**
Rejected because it would break configuration caching support. Gradle requires all configuration to be captured at configuration time for proper caching.

**Alternative 2: Project Dependencies Only**
Rejected as the specification requires both project and external dependencies. The implementation handles both by traversing the complete resolution result.

**Alternative 3: Single Configuration Report**
Rejected in favor of reporting all resolvable configurations, providing more comprehensive dependency visibility.


