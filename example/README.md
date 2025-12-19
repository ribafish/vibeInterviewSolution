# Project Report Plugin - Example Multi-Project Build

This example demonstrates the Project Report Plugin in a realistic multi-project Gradle build.

## Project Structure

```
example-application/
├── app/              - Main application (depends on lib-core and lib-utils)
├── lib-core/         - Core business logic (depends on lib-utils)
└── lib-utils/        - Utility library (no internal dependencies)
```

## Dependencies

**lib-utils**:
- commons-lang3:3.12.0
- guava:32.1.3-jre
- JUnit 5 (test)

**lib-core**:
- Project dependency: lib-utils
- slf4j-api:2.0.9
- jackson-databind:2.15.3
- JUnit 5 + Mockito (test)

**app**:
- Project dependencies: lib-core, lib-utils
- slf4j-simple:2.0.9
- commons-text:1.10.0
- ktor-server-core:2.3.5
- JUnit 5 + MockK (test)

## Running the Example

### Generate Consolidated Multi-Project Report

```bash
./gradlew projectReport
```

This generates **four** reports:
1. **Consolidated report** at `build/reports/multi-project-report.md` - Contains all 3 subprojects in one report
2. Individual report at `lib-utils/build/reports/project-report.md`
3. Individual report at `lib-core/build/reports/lib-core-report.md`
4. Individual report at `app/build/reports/app-dependencies-report.md`

The consolidated report shows all subprojects together with their dependencies, making it easy to understand the complete project structure at a glance.

### Generate Report for Specific Subproject

```bash
./gradlew :app:projectReport
./gradlew :lib-core:projectReport
./gradlew :lib-utils:projectReport
```

### Build the Entire Project

```bash
./gradlew build
```

### Run the Application

```bash
./gradlew :app:run
```

## Plugin Configuration Examples

### Multi-Project Report (Root Project)

Apply the plugin to the root `build.gradle.kts` to generate a consolidated report:

```kotlin
plugins {
    id("com.gradle.project-report")
}

projectReport {
    renderDependencies.set(true)
    output.set(layout.buildDirectory.file("reports/multi-project-report.md"))
}
```

This automatically detects all subprojects and includes them in a single consolidated report.

### Default Configuration (lib-utils)

```kotlin
projectReport {
    renderDependencies.set(true)  // default
    output.set(layout.buildDirectory.file("reports/project-report.md"))  // default location
}
```

### Custom Output Location (lib-core)

```kotlin
projectReport {
    renderDependencies.set(true)
    output.set(layout.buildDirectory.file("reports/lib-core-report.md"))
}
```

### Custom Output with Different Name (app)

```kotlin
projectReport {
    renderDependencies.set(true)
    output.set(layout.buildDirectory.file("reports/app-dependencies-report.md"))
}
```

### Disable Dependency Rendering

```kotlin
projectReport {
    renderDependencies.set(false)  // Only project metadata, no dependencies
}
```

## What to Look For in Generated Reports

### Multi-Project Report

The consolidated report (`build/reports/multi-project-report.md`) contains:
1. **Title**: The root project name with subproject count
2. **Project Sections**: One section per subproject with:
   - Project metadata (name, group, description)
   - All dependencies organized by configuration
3. **Separators**: Each subproject section is separated by a horizontal rule
4. **Complete View**: All subprojects and their dependencies in one file

### Individual Subproject Reports

Each individual report will contain:

1. **Title**: The subproject name (e.g., "# app", "# lib-core")
2. **Project Information**: Group, name, and description
3. **Dependencies**: All resolved dependencies organized by configuration:
   - compileClasspath
   - runtimeClasspath
   - testCompileClasspath
   - testRuntimeClasspath
   - And other configurations created by applied plugins

### Notable Features

- **Project Dependencies**: See how `:lib-core` appears in the app report
- **Transitive Dependencies**: See how dependencies from lib-utils flow into lib-core and app
- **Configuration-specific**: Different configurations show different dependency sets
- **Artifact Names**: Each dependency shows the actual JAR filename

## Build Scans

Build scans are automatically published. After running any Gradle command, check the output for the build scan URL.
