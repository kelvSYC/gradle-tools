# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

All commands are run from the repository root via the Gradle wrapper:

```bash
./gradlew :build          # Build all components + generate BOM, catalog, docs, coverage
./gradlew :assemble       # Compile and package all components (no test/coverage)
./gradlew :test           # Run tests across all components (aggregated report)
./gradlew :jacoco         # Generate JaCoCo coverage report
./gradlew :publish        # Publish all components + BOM + catalog to GitHub Packages
./gradlew :dokkaGenerate  # Generate aggregated HTML documentation
./gradlew :clean          # Clean all components
```

To work on a single component, run Gradle within its directory:

```bash
cd cores/artifactory-base
./gradlew :build          # Build only this component
./gradlew :test           # Run only this component's tests
./gradlew :detekt         # Lint only this component
```

Publishing requires `GITHUB_ACTOR` and `GITHUB_TOKEN` environment variables.

## Architecture

This is a **composite build** of Gradle plugins and Kotlin libraries, published to GitHub Packages (`maven.pkg.github.com/kelvSYC/gradle-tools`).

### Build Hierarchy

The root `settings.gradle.kts` composes three layers of included builds:

1. **`gradle/`** — Internal build infrastructure (not published):
   - `gradle/platform` — A `java-platform` BOM that centralizes all dependency versions for use across all component builds
   - `gradle/settings` — A settings plugin (`com.kelvsyc.internal`) that applies the semver plugin and wires up the platform/catalog for every component build
   - `gradle/plugins` — Convention plugins used by component builds:
     - `com.kelvsyc.internal.kotlin-library` — JVM library
     - `com.kelvsyc.internal.kotlin-gradle-library` — Gradle API library
     - `com.kelvsyc.internal.kotlin-plugin` — Gradle plugin (uses `kotlin-dsl`)
     - `com.kelvsyc.internal.kotlin-multiplatform-jvm-library` — Kotlin Multiplatform (JVM target)
     - `com.kelvsyc.internal.github-publishing` — Publishes to GitHub Packages
     - `com.kelvsyc.internal.jacoco` / `kotlin-multiplatform-jacoco` — JaCoCo wiring
     - `com.kelvsyc.internal.dokka` / `dokkatoo` — Dokka/Dokkatoo wiring

2. **`cores/`** — Published Gradle plugins (group `com.kelvsyc.gradle`). Each is an independent included build with its own `settings.gradle.kts` that applies `com.kelvsyc.internal` and declares inter-core dependencies. Plugin types:
   - `*-extensions` — Gradle API utility libraries (not plugins per se)
   - `clients-base` — Base service for managing SDK clients (prerequisite for most AWS/GCP plugins)
   - `*-base` — Gradle plugins for specific cloud services (Artifactory, AWS S3/SQS/SNS/SES/SecretsManager/CodeArtifact/IMDS, GCP Artifact Registry/Storage, Git)

3. **`extensions/`** — Published Kotlin libraries (group `com.kelvsyc.kotlin` for `kotlin-core`, `com.kelvsyc.gradle` for others). Pure Kotlin extension functions; no Gradle plugin API.

4. **`aggregation/`** — Aggregation build (not independently published artifacts, but publishes BOM and catalog):
   - `bom` — `java-platform` BOM covering all `cores/` and `extensions/` artifacts
   - `catalog` — Version catalog (`.toml`) covering the same
   - `dokka` — Aggregated Dokka documentation
   - `jacoco` — Aggregated JaCoCo coverage
   - `testing` — Aggregated test reports

### Component Settings Pattern

Every component's `settings.gradle.kts` follows this pattern:
```kotlin
pluginManagement { includeBuild("../../gradle/settings") }
plugins { id("com.kelvsyc.internal") }
includeBuild("../clients-base")  // inter-core dependency, as needed
```

### Convention Plugin Application

Every component's `build.gradle.kts` applies convention plugins:
- Libraries: `com.kelvsyc.internal.kotlin-library` or `com.kelvsyc.internal.kotlin-multiplatform-jvm-library`
- Gradle plugins: `com.kelvsyc.internal.kotlin-plugin`
- Plus `com.kelvsyc.internal.dokka`, `com.kelvsyc.internal.jacoco`, `com.kelvsyc.internal.github-publishing`

### Versioning

Versioning uses the `com.javiersc.semver` plugin configured in `aggregation/settings.gradle.kts`, driven by git tags. The version is shared across all components via the composite build mechanism.

### Testing

Tests use [Kotest](https://kotest.io/) with JUnit Platform. Gradle plugin tests use the `gradle-testkit-jacoco` plugin for TestKit + JaCoCo integration.

### Detekt

Detekt 1.23.x is used for linting. Because the project targets JVM 25 but Detekt 1.23.x only supports `--jvm-target` up to 22, all four convention plugins that apply Detekt explicitly set:
```kotlin
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "22"
}
```
Config is at `gradle/detekt.yml`.
