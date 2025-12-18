# Implementation Plan

Detailed plan for the Gradle project report plugin (Kotlin-first).

## 1. Project Setup
- Initialize Gradle plugin project using Kotlin DSL and Kotlin sources.
- Configure `settings.gradle.kts` with root project name and wrapper using current stable Gradle.
- Apply `java-gradle-plugin`, `kotlin("jvm")`, and `com.gradle.enterprise` (for always-on scans).
- Set `group`/`version`, plugin id, repositories (`mavenCentral()`), Kotlin JVM target.

## 2. Extension API (`projectReport`)
- Create `ProjectReportExtension` with:
  - `renderDependencies: Property<Boolean>` default `true`.
  - `outputFile: RegularFileProperty` default `layout.buildDirectory.file("reports/project-report.md")`.
- Optional validation (ensure parent dirs can be created).

## 3. Task Type (`ProjectReport`)
- Implement Kotlin task annotated `@CacheableTask`.
- Inputs via Providers: project metadata (`group`, `name`, `description`), `renderDependencies`.
- Output: `RegularFileProperty outputFile`.
- Task action:
  - Build Markdown with title (project name), metadata section.
  - Conditionally render Dependencies section.
  - Write to `outputFile` (create parents).

## 4. Dependency Rendering Logic
- Iterate all configurations; skip non-resolvable ones.
- Use `configuration.incoming` with an `artifactView` (lenient) to access resolved components/files.
- Collect project and external dependencies flattened per configuration.
- Format `<group>:<artifact>:<version> - <artifactFileNameOrDirName>`.
- Deterministic ordering: sort by configuration name then by coordinate string; handle missing coords (e.g., use project path).

## 5. Plugin Wiring
- In `ProjectReportPlugin.apply`:
  - Register extension under name `projectReport`.
  - Register `projectReport` task of type `ProjectReport`.
  - Lazily wire task properties from extension (`map`/`flatMap`), avoid eager configuration.
  - Set task description/group.
- Ensure execution phase avoids project mutation (configuration-cache friendly).

## 6. Performance Features
- Build cache: annotate inputs/outputs, keep deterministic content.
- Configuration cache: avoid `Project` access in action, only serialized fields.
- Lazy configuration: Provider-based access; do not iterate configurations before task runs.

## 7. Build Scan Always-On
- Configure `gradleEnterprise` to publish to `scans.gradle.com` with TOS acceptance.
- Optionally allow opt-out via property/env, but default to always-on per requirements.

## 8. Testing Strategy (Gradle TestKit + unit)
- Functional TestKit cases:
  - Metadata-only project (no deps → only metadata section).
  - Single external dep (commons-lang3 example).
  - External with transitive (commons-text → two lines).
  - Project dependency case (project path/coords rendered).
  - `renderDependencies = false` skips section.
  - Custom `outputFile` honored.
  - Optional: configuration-cache reuse check.
- Unit tests for Markdown builder if factored out.

## 9. Multi-Gradle Verification
- Parameterize TestKit runs for latest minor/patch of the three most recent major Gradle versions (e.g., 7.x, 8.x, 9.x constants).

## 10. CI Workflow
- `.github/workflows/build.yaml`:
  - Trigger on push/PR.
  - Use `actions/setup-java`, `gradle/actions/setup-gradle`.
  - Matrix over Gradle versions.
  - Run `./gradlew check`.
  - Upload test reports on failure; rely on build scans for diagnostics.

## 11. Packaging
- Add task or script to create `project.zip` excluding `build/` outputs; ensure source + workflow included.

## 12. Documentation
- README: summarize decisions, rendering approach, performance strategies, Gradle version matrix rationale, testing, build scan always-on note, AI/tooling disclosure, link to this plan.
