# Project Report Gradle Plugin

Kotlin-first Gradle plugin that generates a Markdown report with project metadata and resolved dependencies.

## Usage
- Apply the plugin id `com.vibe.projectreport`.
- Optional extension settings:
  - `renderDependencies` (default `true`) to toggle dependency rendering.
  - `outputFile` (default `build/reports/project-report.md`) to change where the report is written.

## Report format
- Title: `# <projectName> Project Report`
- Metadata section lists project name, group, and description (falls back to `None`).
- Dependencies section includes one bullet per resolved artifact as `<configuration>: <group>:<artifact>:<version> - <artifactFile>`. When nothing can be resolved, the section shows `_No resolvable dependencies._`.
- Setting `renderDependencies = false` skips the Dependencies section entirely.

## Development
- Build and test: `./gradlew build`
- Gradle build scans are published to `scans.gradle.com` by default; set `DISABLE_BUILD_SCAN=true` to opt out.
- Functional tests use Gradle TestKit across multiple Gradle versions (currently 8.14.3 and 9.4.0); locally cached distributions are required for the matrix entries.
