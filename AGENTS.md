# Agent Guidance

Guidance for coding agents (and humans) working in this Gradle composite build: requirements, build commands, detekt rules, architecture and gotchas. This file is written to be model-neutral; `CLAUDE.md` only imports it (plus any Claude-specific setup). Keep tool- or vendor-specific instructions out of it.

## Requirements

- **Do not commit intermediary design documents** (e.g., `docs/superpowers/specs/*-design.md`). These are working files used during planning; only the resulting code and component `README.md` belong in the repo.
- **Tests and detekt must pass.** All code changes must pass `./gradlew :test` and `./gradlew :detekt` before a task is complete. Breaking either check is not acceptable.
- **KDocs are required** on every `DefaultTask` subclass, `WorkAction` implementation, and `ValueSource` implementation, including their individual properties and parameters.
- **Every `WorkAction` implementation must have a unit test.** Follow the `MockXyzClientBuildService` pattern: create a test-only service subclass in `src/test/kotlin` that overrides `createClient()` to return a mock client (see `MockSecretsManagerClientBuildService` as reference). Instantiate the action as an anonymous subclass with `getParameters()` overridden, then call `execute()` directly. WorkActions that call external services (AWS, GCP, Azure, Vault) are fully unit-testable against mocked clients — do not defer tests on the grounds that integration tests are required.
- **Every `cores/` component must have a `README.md`** — it is not optional. Create it when scaffolding a new component and keep it current when adding or modifying public-facing API (tasks, `WorkAction`/`ValueSource` implementations, extensions, plugin behaviour).
- **README must stay current.** Any change that adds or significantly modifies a public-facing component — including Gradle tasks, `WorkAction` implementations, `ValueSource` implementations, extensions, or plugin behaviour — must include a corresponding update to the component's `README.md`.
- **Root README must stay current.** When adding a new component, add it to the appropriate table in the root `README.md` (cores, bases, or extensions).
- **Be concise.** Avoid unnecessary explanation or narrative.
- **No unsolicited explanations.** Only explain what you're doing if explicitly asked.

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

The first `:test` run downloads Gradle 8.14, 9.0.0 and 9.4.1 for aggregation/compatibility tests, so it needs network access.

## Detekt Rules (avoid failures before validation)

Config is at `gradle/detekt.yml`. Rules most likely to trip up generated code:

- **`NewLineAtEndOfFile`** — every `.kt` file must end with a newline character. **This is the single most common cause of detekt failures on generated files.** See the mandatory procedure below.
- **`TooGenericExceptionCaught`** — do not catch `Exception`, `RuntimeException`, `Error`, `Throwable`, `NullPointerException`, `IndexOutOfBoundsException`, or `IllegalMonitorStateException`. Catch the specific exception type thrown by the API.
- **`TooGenericExceptionThrown`** — do not throw `Exception`, `RuntimeException`, `Error`, or `Throwable`.
- **`WildcardImport`** — no wildcard imports (only `java.util.*` is allowed).
- **`SwallowedException`** — caught exceptions must be used (e.g. passed to a logger); silent catch blocks are forbidden.
- **`ForbiddenComment`** — do not write `TODO:`, `FIXME:`, or `STOPSHIP:` markers.
- **`MagicNumber`** — no unexplained numeric literals in non-test, non-`.kts` source; extract to named constants.
- **`ReturnCount`** — max 2 `return` statements per function.
- **`UnusedPrivateMember` / `UnusedPrivateProperty` / `UnusedPrivateClass`** — remove unused private declarations.

### Trailing newline: mandatory procedure ⚠️

**File-writing tools strip trailing newlines.** A content string ending with `\n` does NOT produce a file ending with `\n`. You must fix this immediately after every Write call on a `.kt` file — not at the end of the task, not as a checklist step, but as the very next action:

```bash
echo "" >> path/to/File.kt
```

If you write multiple `.kt` files in one step, append a newline to each one before moving on:

```bash
echo "" >> File1.kt && echo "" >> File2.kt && echo "" >> File3.kt
```

Verify with `tail -c1 <file> | xxd` — output must be `0a`. Detekt will fail the build if even one file is missing the final newline.

### Pre-completion checklist for Kotlin file changes

Before declaring any task done, run through this list for every `.kt` file you created or modified:

1. **Trailing newline** — `tail -c1 <file> | xxd` outputs `0a`. Fix with `echo "" >> <file>`.
2. **No wildcard imports** — no `import foo.bar.*` (except `java.util.*`).
3. **No magic numbers** in non-test, non-`.kts` source.
4. **No generic exception types** caught or thrown.
5. **No unused private members**.
6. **No TODO/FIXME/STOPSHIP comments**.

## Architecture

This is a **composite build** of Gradle plugins and Kotlin libraries, published to GitHub Packages (`maven.pkg.github.com/kelvSYC/gradle-tools`).

### Build Hierarchy

The root `settings.gradle.kts` composes three layers of included builds:

1. **`gradle/`** — Internal build infrastructure (not published):
   - `gradle/settings` — Thin settings plugin (`com.kelvsyc.internal.gradle-tools.settings`): applies the shared settings plugin `com.kelvsyc.internal.settings` from [gradle-build-conventions](https://github.com/kelvSYC/gradle-build-conventions) (repositories, the `libs` catalog and platform `libs.conventions.platform`, semver), includes `gradle/plugins` and registers `gradle/libs.versions.toml` as the `local` catalog
   - `gradle/plugins` — Convention plugins (IDs `com.kelvsyc.internal.gradle-tools.*`): `kotlin-gradle-library`, `kotlin-gradle-plugin`, `gradle-integration-test`, `github-publishing`, `jacoco`, `dokka`. The `github-publishing` plugin strips the internal platform BOM (`com.kelvsyc.internal:platform`) from published POM and Gradle module metadata so that consumers don't see an unresolvable dependency. The convention plugins still declare `implementation(platform(...))` for local resolution — the stripping happens at publication time via `pom.withXml` and a `GenerateModuleMetadata` post-processing action (`StripInternalPlatform`).

2. **`cores/`** — Published Gradle plugins (group `com.kelvsyc.gradle`). Each is an independent included build. Types:
   - `*-extensions` — Gradle API utility libraries
   - `clients-base` — Base service for SDK client management (prerequisite for most AWS/GCP plugins)
   - `*-base` — Plugins for specific cloud services (Artifactory, AWS S3/SQS/SNS/SES/SecretsManager/CodeArtifact/IMDS, GCP Artifact Registry/Storage, Git)

3. **`aggregation/`** — Publishes BOM and version catalog; aggregates Dokka docs, JaCoCo coverage, and test reports.

### Component Settings Pattern

Every component's `settings.gradle.kts`:
```kotlin
pluginManagement {
    includeBuild("../../gradle/settings")
    repositories { /* identical GitHub Packages block in every settings file */ }
}
plugins { id("com.kelvsyc.internal.gradle-tools.settings") }
includeBuild("../clients-base")  // inter-core dependency, as needed
```

### Testing

Tests use [Kotest](https://kotest.io/) with JUnit Platform. Gradle plugin tests use the `gradle-testkit-jacoco` plugin for TestKit + JaCoCo integration. Versioning is driven by git tags via the shared semver settings plugin (`com.kelvsyc.internal.semver`, applied by the shared settings plugin).

### JDK Constraints (do not change)

The Gradle daemon is pinned to JDK 21 (`gradle/gradle-daemon-jvm.properties`). **Do not change `toolchainVersion` to 25** — Detekt 1.23.x cannot parse JVM version "25.x". Convention plugins override `jvmTarget = "22"` and `jdkHome` to JDK 21 for detekt tasks.

`cores/` components target JDK 21. **Use LTS JDKs only; a core goes above JDK 21 only if it needs newer Java for its own reasons.** Lowering the target widens the range of Gradle versions and JDKs a core works on; raising it strands consumers.

The Kotlin version (`kotlin` in `gradle/libs.versions.toml`) must equal the Gradle wrapper's embedded Kotlin version (see the [compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html#kotlin)). `gradle-kotlin-dsl-plugins` pins `kotlin-stdlib` strictly to that version, so a higher catalog value fails dependency resolution and a lower one is silently overridden. Bump both together; Dependabot ignores Kotlin for this reason.

Tests run on JDK 21 via toolchain. Convention plugins wire the required mockk/ByteBuddy JVM args (`-XX:+EnableDynamicAgentLoading`, `-Dnet.bytebuddy.experimental=true`, `-javaagent:[byte-buddy-agent]`). **Do not remove these.**

### Gradle Compatibility Tiers

Each core has a minimum Gradle, set by `com.kelvsyc.gradle.minimumGradle` in the core's `gradle.properties` (absent means `8.14`) and defined in `gradle/plugins/kotlin-convention/src/main/kotlin/GradleTier.kt`. There are two tiers:

| Tier | Cores | Compiled against | Kotlin `apiVersion`/`languageVersion` |
|------|-------|------------------|---------------------------------------|
| 8.14 (default) | 49 | `org.gradle.experimental:gradle-public-api:8.14` | 2.0 (Gradle 8.14 embeds Kotlin 2.0.21) |
| 9.0 | the 13 `aws-*-kotlin-base`, `aws-kotlin-extensions`, `google-cloud-artifact-registry-base` | `gradle-public-api:9.0.0` | 2.2 |

- **The rule:** the build's own tooling (convention plugins, settings, build scripts) uses the newest Kotlin the wrapper's Gradle allows; published code compiles against its tier's baseline.
- Kotlin language 2.0 is deprecated as of Kotlin 2.4. When a future Gradle ships a Kotlin that drops 2.0, the 8.14 tier must be revisited.
- The 9.0 tier's floor comes from third-party code: `kotlinx-coroutines` 1.11 and the AWS SDK need Kotlin stdlib classes first shipped in Gradle 9.0. These cores support build scripts from Gradle 9.0, but Kotlin-source build-logic (e.g. `kotlin-dsl` convention plugins) only from Gradle 9.4, because the AWS SDK ships Kotlin 2.4 metadata that the Kotlin 2.2 compiler in Gradle 9.0 to 9.3 cannot read. That Kotlin-source minimum follows the Kotlin version the AWS SDK is built with, so SDK upgrades can raise it.
- `gradle-extensions` additionally publishes a `gradle99` variant (Gradle 9.9+, Kotlin 2.4) with its existing deprecations.
- Each core publishes its tier as `org.gradle.plugin.api-version`, so an older Gradle rejects it at dependency resolution rather than failing at run time.
- Published dependencies exclude `kotlin-stdlib`, `-jdk7`, `-jdk8` and `kotlin-reflect`; Gradle supplies them.
- `verifyGradleLinkage` (a task in each core, run by its `check` and by root `./gradlew :check`) checks every reference from a core and every jar it ships against its tier's minimum Gradle API and that Gradle's Kotlin stdlib/reflect. The report is at `build/reports/gradle-linkage/report.txt`.
- `aggregation/compat` (`TierCompatibilitySpec`, run by root `:test`) publishes a few cores to `build/compat-repo` and runs real Gradle distributions via TestKit: consumer resolution (including Kotlin-source build-logic with `gradle-kotlin-dsl-plugins`), rejection of a 9.0-tier core on 8.14, and one runtime smoke test per tier. Root `:test` downloads Gradle 8.14, 9.0.0 and 9.4.1 the first time, so it needs network.
- `GradleVariantSelectionSpec` (`gradle-extensions`) and `DokkaDashGradleVersionSpec` (`dokka-dash`) test their cores across Gradle versions.

## Critical Gotchas

### Detekt + JDK 25 = FAILS ⚠️

The Gradle daemon is pinned to JDK 21 (`gradle/gradle-daemon-jvm.properties`). **Do not change to 25.**

Detekt 1.23.x's bundled Kotlin compiler cannot parse JVM version "25.x" and throws `IllegalArgumentException`. Workaround is already in place in convention plugins (`jvmTarget = "22"` and `jdkHome` overrides) — don't remove them.

### Test JVM Args (ByteBuddy / mockk) ⚠️

Tests run on JDK 21, and mockk/ByteBuddy requires these JVM args (already wired in convention plugins):
```
-XX:+EnableDynamicAgentLoading
-Dnet.bytebuddy.experimental=true
-javaagent:[byte-buddy-agent jar path]
```

Do not remove these from convention plugins.

### Publishing Requires Env Vars ⚠️

`./gradlew :publish` requires `GITHUB_ACTOR` and `GITHUB_TOKEN`. Without them, it fails silently or with obscure errors.

### Component Settings Pattern ⚠️

Do not modify the `pluginManagement`/`plugins` lines in each component's `settings.gradle.kts` (see Component Settings Pattern under Architecture) — they wire up the composite build.

## Configuration Cache (CC) Rules for BuildService Components ⚠️

These rules apply whenever you create or modify a `*-base` component that contains a `BuildService`.

### Rule 1 — `BuildServiceParameters` must be CC-serializable

Only `Property<T>` where T is a primitive, `String`, enum, or `java.io.Serializable` type is safe. **Never place SDK types directly on parameters** — they are not CC-serializable:

| Forbidden | Replacement |
|---|---|
| `Property<PasswordCredentials>` | `Property<String>` username + `Property<String>` password |
| `Property<Region>` (AWS) | `Property<String>` regionId |
| `Property<CredentialsProvider>` | `Property<String>` accessKeyId + secretAccessKey + sessionToken + `Property<AwsCredentialSource>` |
| `Property<Credentials>` (GCP) | `Property<String>` credentialsJson, or use a `CredentialSource` enum |
| `Property<TokenCredential>` (Azure) | `Property<String>` clientId + tenantId + clientSecret / clientCertificate |

Reconstruct the SDK object inside `createClient()` using the stored string parameters.

### Rule 2 — Nested `BuildService` refs must be registered providers

When a `WorkAction` or task parameter holds a `Property<SomeBuildService>`, it **must** be set via the provider returned from `gradle.sharedServices.registerIfAbsent(...)`. Passing an instance created via `ObjectFactory.newInstance()` bypasses registration and breaks CC because Gradle stores the service name to re-resolve it across cache hits.

### Rule 3 — Every new `*-base` component with a BuildService must ship a CC probe

When adding a new `*-base` component that defines a `BuildService`:

1. Apply `id("com.kelvsyc.internal.gradle-tools.gradle-integration-test")` in the component's `build.gradle.kts`.
2. Create `src/integrationTest/kotlin/.../fixtures/<Service>ProbeTask.kt` — a minimal `DefaultTask` that holds `@get:Internal abstract val service: Property<YourBuildService>` and calls `service.get()` in its `@TaskAction`.
3. Create `src/integrationTest/kotlin/.../BuildServiceConfigurationCacheSpec.kt` — at minimum, one test case with no parameters to prove the empty-params round-trip works. Follow the pattern established in `artifactory-base` and `bitbucket-cloud-base`.
4. Add the component name to **both**:
   - The `integrationTestComponents` set in `aggregation/testing/build.gradle.kts`
   - The `Integration Tests` step in `.github/workflows/gradle-build.yml`

Skipping any of these four steps means CC breakage will be invisible in CI.

## Quick Navigation

- **Architecture & detailed build hierarchy**: see Architecture above
- **Linting config**: `gradle/detekt.yml`
- **Source code structure**: `cores/*/src/main/kotlin/`
- **Tests**: Mirror `src/main` structure under `src/test/`
