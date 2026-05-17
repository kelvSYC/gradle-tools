# CLAUDE.md

Architectural reference for this Gradle composite build. See `AGENTS.md` for build commands and detekt gotchas.

## Requirements

- **Do not commit intermediary design documents** (e.g., `docs/superpowers/specs/*-design.md`). These are working files used during planning; only the resulting code and component `README.md` belong in the repo.

- **Tests and detekt must pass** before any task is complete: `./gradlew :test` and `./gradlew :detekt`.
- **KDocs are required** on every `DefaultTask` subclass, `WorkAction` implementation, and `ValueSource` implementation, including their individual properties and parameters.
- **Every `WorkAction` implementation must have a unit test.** Follow the `MockXyzClientBuildService` pattern: create a test-only service subclass in `src/test/kotlin` that overrides `createClient()` to return a mock client (see `MockSecretsManagerClientBuildService` as reference). Instantiate the action as an anonymous subclass with `getParameters()` overridden, then call `execute()` directly. WorkActions that call external services (AWS, GCP, Azure, Vault) are fully unit-testable against mocked clients — do not defer tests on the grounds that integration tests are required.
- **Every `cores/` component must have a `README.md`** — it is not optional. Create it when scaffolding a new component and keep it current when adding or modifying public-facing API (tasks, `WorkAction`/`ValueSource` implementations, extensions, plugin behaviour).
- **Root README must stay current** when adding a new component — add it to the appropriate table in `README.md` (cores, bases, or extensions).

## Build Commands

```bash
./gradlew :build          # Build all components + BOM, catalog, docs, coverage
./gradlew :check          # Run tests + detekt across all components (no docs/coverage)
./gradlew :test           # Run tests across all components
./gradlew :detekt         # Lint all components
./gradlew :publish        # Publish to GitHub Packages (requires GITHUB_ACTOR, GITHUB_TOKEN)
```

Single component (from repo root):
```bash
./gradlew :<component>:build
./gradlew :<component>:test
./gradlew :<component>:detekt
```

## Detekt Rules

Config: `gradle/detekt.yml`. Trailing newlines on `.kt` files are enforced by the PostToolUse hook. Other active rules:

- **`TooGenericExceptionCaught`** — catch specific types; forbidden: `Exception`, `RuntimeException`, `Error`, `Throwable`, `NullPointerException`, `IndexOutOfBoundsException`, `IllegalMonitorStateException`.
- **`TooGenericExceptionThrown`** — do not throw `Exception`, `RuntimeException`, `Error`, or `Throwable`.
- **`WildcardImport`** — no wildcard imports (only `java.util.*` is permitted).
- **`SwallowedException`** — caught exceptions must be referenced (e.g. passed to a logger).
- **`ForbiddenComment`** — no `TODO:`, `FIXME:`, or `STOPSHIP:` markers.
- **`MagicNumber`** — no unexplained numeric literals in non-test, non-`.kts` source; use named constants.
- **`ReturnCount`** — max 2 `return` statements per function.
- **`UnusedPrivateMember` / `UnusedPrivateProperty` / `UnusedPrivateClass`** — remove unused private declarations.

## Architecture

This is a **composite build** of Gradle plugins and Kotlin libraries, published to GitHub Packages (`maven.pkg.github.com/kelvSYC/gradle-tools`).

### Build Hierarchy

The root `settings.gradle.kts` composes three layers of included builds:

1. **`gradle/`** — Internal build infrastructure (not published):
   - `gradle/platform` — BOM centralizing all dependency versions
   - `gradle/settings` — Settings plugin (`com.kelvsyc.internal`) wiring platform/catalog into every component build
   - `gradle/plugins` — Convention plugins: `kotlin-library`, `kotlin-gradle-library`, `kotlin-plugin`, `kotlin-multiplatform-jvm-library`, `github-publishing`, `jacoco`, `dokka`. The `github-publishing` plugin strips the internal platform BOM (`com.kelvsyc.internal:platform`) from published POM and Gradle module metadata so that consumers don't see an unresolvable dependency. The convention plugins still declare `implementation(platform(...))` for local resolution — the stripping happens at publication time via `pom.withXml` and a `GenerateModuleMetadata` post-processing action (`StripInternalPlatform`).

2. **`cores/`** — Published Gradle plugins (group `com.kelvsyc.gradle`). Each is an independent included build. Types:
   - `*-extensions` — Gradle API utility libraries
   - `clients-base` — Base service for SDK client management (prerequisite for most AWS/GCP plugins)
   - `*-base` — Plugins for specific cloud services (Artifactory, AWS S3/SQS/SNS/SES/SecretsManager/CodeArtifact/IMDS, GCP Artifact Registry/Storage, Git)

3. **`aggregation/`** — Publishes BOM and version catalog; aggregates Dokka docs, JaCoCo coverage, and test reports.

### Component Settings Pattern

Every component's `settings.gradle.kts`:
```kotlin
pluginManagement { includeBuild("../../gradle/settings") }
plugins { id("com.kelvsyc.internal") }
includeBuild("../clients-base")  // inter-core dependency, as needed
```

### Testing

Tests use [Kotest](https://kotest.io/) with JUnit Platform. Gradle plugin tests use the `gradle-testkit-jacoco` plugin for TestKit + JaCoCo integration. Versioning is driven by git tags via `com.javiersc.semver`.

### JDK Constraints (do not change)

The Gradle daemon is pinned to JDK 21 (`gradle/gradle-daemon-jvm.properties`). **Do not change `toolchainVersion` to 25** — Detekt 1.23.x cannot parse JVM version "25.x". Convention plugins override `jvmTarget = "22"` and `jdkHome` to JDK 21 for detekt tasks; `cores/` components still compile to JDK 25 via a forked toolchain process.

Tests run on JDK 25 via toolchain. Convention plugins wire the required mockk/ByteBuddy JVM args (`-XX:+EnableDynamicAgentLoading`, `-Dnet.bytebuddy.experimental=true`, `-javaagent:[byte-buddy-agent]`). **Do not remove these.**
