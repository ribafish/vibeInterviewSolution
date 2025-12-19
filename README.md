# Project Report Gradle Plugin

Kotlin-first Gradle plugin that generates a Markdown report with project metadata and resolved dependencies.

## Usage
- Apply plugin id `com.vibe.projectreport`.
- Extension `projectReport`:
  - `renderDependencies` (default `true`) toggles the Dependencies section.
  - `outputFile` (default `build/reports/project-report.md`) sets the report location.

## Report format
- Title: `# <projectName>`
- Metadata: name, group, description (falls back to `None`).
- Dependencies: when enabled and resolvable entries exist, each line is `<group>:<artifact>:<version> - <artifactFileOrDirName>`.

## Build, test, package
- Build and test: `./gradlew check`
- Package source (excludes build output): `./gradlew packageProject` → `project.zip` in repo root.
- Gradle build scans always publish to `scans.gradle.com` for every build (Develocity plugin in `settings.gradle.kts`).
- Tested with Gradle 7.6.4, 8.14.3, and 9.2.1 via TestKit and CI matrix.

## Example multi-project usage
- Location: `examples/multi-project` (modules: `app`, `library`, `utils`), plugin applied at the root `build.gradle.kts` to generate a single merged report.
- How it works: the root applies the Java plugin and declares normal `implementation` dependencies on each subproject; their external dependencies flow into the resolved graph automatically.
- Run: `./gradlew -p examples/multi-project projectReport` (uses composite build via `includeBuild("..")` to source the plugin).
- Inspect report: `examples/multi-project/build/reports/project-report.md` contains project metadata and a combined dependency list (e.g., `:app`, `:library`, `:utils`, `commons-text`, `commons-lang3`).
- Tweak the example: modify subproject dependencies or descriptions, rerun the command, and re-open the merged report.

## Decisions and trade-offs
- Dependencies are flattened and sorted lexically without configuration prefixes to match the provided expected output.
- Dependencies section is omitted entirely when nothing is resolvable, per verification guidance.
- Lenient artifact view is used to avoid hard failures on partially resolvable graphs while still listing what can be resolved.
- Build scans are enforced (no opt-out) to satisfy the requirement of publishing on every build.
- Added a dedicated packaging task instead of committing an archive to keep the repo clean while meeting the `project.zip` requirement.

## Tooling / AI usage
- Used ChatGPT (via Codex CLI) for implementation guidance and code edits; all changes were reviewed in this repository context.
