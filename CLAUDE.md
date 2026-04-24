# CLAUDE.md

Comprehensive reference for building, testing, and publishing in this Gradle composite build. This document provides architectural context and detailed guidance for all agents (human and automated).

**Quick reference for automated agents:** See `AGENTS.md` for critical gotchas and a condensed build commands guide.

## Style & Expectations

- **Be concise.** Avoid unnecessary elaboration or summaries. Show the work, not the narrative.
- **Explanations only when prompted.** Don't explain what you're doing or why unless asked.
- **Tests and detekt must pass.** All code changes must pass `./gradlew :test` and `./gradlew :detekt` before being considered complete. Never commit or propose code that breaks either check.
- **README must stay current.** Any change that adds or significantly modifies a public-facing component — including Gradle tasks, `WorkAction` implementations, `ValueSource` implementations, extensions, or plugin behaviour — must include a corresponding update to the component's `README.md`.

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

To work on a single component, use the included build form from the repository root:

```bash
./gradlew :artifactory-base:build    # Build only this component
./gradlew :artifactory-base:test     # Run only this component's tests
./gradlew :artifactory-base:detekt   # Lint only this component
```

All builds must pass a detekt check. When adding or modifying Kotlin source files, run `./gradlew :detekt` (or `./gradlew :<component-name>:detekt` for a single component) and fix any reported issues before considering the build complete.

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
     - `com.kelvsyc.internal.dokka` — Dokka wiring

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

Detekt 1.23.x is used for linting. There are two interacting constraints:

1. **Daemon JVM**: `gradle/gradle-daemon-jvm.properties` pins the Gradle daemon to JDK 21 (`toolchainVersion=21`). Detekt runs in-process on the daemon, and Detekt 1.23.x's bundled Kotlin compiler calls `JavaVersion.current()` to parse the running JVM version — it cannot parse "25.0.2" and throws `IllegalArgumentException`. **Do not change `toolchainVersion` to 25.**

   The internal build infrastructure (`gradle/settings`, `gradle/plugins/*`) all use `jvmToolchain(21)` so their class files load correctly into the JDK 21 daemon. Published component builds (`cores/`, `extensions/`) still compile with JDK 25 via toolchain (a forked process), so only their output artifacts target JVM 25.

2. **`--jvm-target` and `--jdk-home`**: Detekt auto-wires both from the `JavaPluginExtension` toolchain (which Kotlin sets to JDK 25). Detekt 1.23.x does NOT have a `javaLauncher` property — it has `jdkHome: DirectoryProperty` and `jvmTarget`. All four convention plugins override both:
```kotlin
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "22"
    jdkHome.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    }.map { it.metadata.installationPath })
}
```

Config is at `gradle/detekt.yml`.

### Test JVM compatibility (mockk / ByteBuddy)

Tests run on JDK 25 via toolchain (forked process). mockk uses ByteBuddy for mocking. Three JVM args are required in all four convention plugins:

```kotlin
tasks.withType<Test>().configureEach {
    jvmArgs("-XX:+EnableDynamicAgentLoading")   // allow ByteBuddy to self-attach on JVM 21+
    jvmArgs("-Dnet.bytebuddy.experimental=true") // ByteBuddy 1.15.x only officially supports up to Java 24
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        // Pre-attach the agent at startup — dynamic self-attachment is unreliable on JVM 25
        classpath.find { "byte-buddy-agent" in it.name }
            ?.let { listOf("-javaagent:$it") }
            ?: emptyList()
    })
}
```

The `javaagent` provider is a no-op for components that don't have mockk on their test classpath.
