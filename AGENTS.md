# Agent Guidance

Guidance for coding agents (and humans) working in this Gradle composite build: requirements, build commands, detekt rules, architecture and gotchas. This file is written to be model-neutral; `CLAUDE.md` only imports it (plus any Claude-specific setup). Keep tool- or vendor-specific instructions out of it.

## Requirements

- **Do not commit intermediary design documents** (e.g., `docs/superpowers/specs/*-design.md`). These are working files used during planning; only the resulting code and component `README.md` belong in the repo.
- **Tests and detekt must pass.** All code changes must pass `./gradlew :test` and `./gradlew :detekt` before a task is complete. Breaking either check is not acceptable.
- **KDocs are required** on every `DefaultTask` subclass, `WorkAction` implementation, and `ValueSource` implementation, including their individual properties and parameters.
- **Every `WorkAction` implementation must have a unit test.** Follow the `MockXyzClientBuildService` pattern: create a test-only service subclass in `src/test/kotlin` that overrides `createClient()` to return a mock client (see `MockSecretsManagerClientBuildService` as reference). Instantiate the action as an anonymous subclass with `getParameters()` overridden, then call `execute()` directly. WorkActions that call external services (AWS, GCP, Azure, Vault) are fully unit-testable against mocked clients — do not defer tests on the grounds that integration tests are required.
- **Every `cores/` component must have a `README.md`** — it is not optional. Create it when scaffolding a new component and keep it current when adding or modifying public-facing API (tasks, `WorkAction`/`ValueSource` implementations, extensions, plugin behaviour).
- **README must stay current.** Any change that adds or significantly modifies a public-facing component — including Gradle tasks, `WorkAction` implementations, `ValueSource` implementations, extensions, or plugin behaviour — must include a corresponding update to the component's `README.md`.
- **Injection style.** Classes not designed to be extended (concrete tasks, `WorkAction`, `ValueSource`, `BuildService`, extensions, helper objects) use constructor injection (`@Inject constructor(...)`), including tasks that are `abstract` only so that Gradle can generate their managed properties. Classes designed to be extended by other code (the `Abstract*` bases and documented extension points) take no constructor parameters and inject their services as `@get:Inject protected abstract val` properties, documented as available to extenders: a constructor parameter makes every extender repeat it, and adding one later breaks them, whereas an added injected property does not. KDoc a class that is `abstract` only for Gradle, but not meant to be extended, as such and point to the base to extend instead (see `AntWork` and `AbstractAntTask` in `cores/ant-bridge`).
- **A core may have an `INTERNALS.md`** for maintainers: the internal plumbing and the reasons behind it that the consumer-facing `README.md` does not cover (see `cores/ant-bridge/INTERNALS.md`). Add one when a core has non-obvious internals that a contributor needs a tour of, link it from the README, and keep it current like the README when that plumbing changes. It is maintained documentation, unlike the intermediary design documents above.
- **Root README must stay current.** When adding a new component, add it to the appropriate table in the root `README.md` (cores, bases, or extensions).
- **Be concise.** Avoid unnecessary explanation or narrative.
- **No unsolicited explanations.** Only explain what you're doing if explicitly asked.

## Build Commands

```bash
./gradlew :build          # Build all components + BOM, catalog, docs, coverage
./gradlew :check          # Tests, compat tests, detekt, verifyGradleLinkage, checkKotlinAbi, licensee and verifyPomDependencyVersions of every component (no docs/coverage)
./gradlew :checkKotlinAbi # Public-API dump check of every component (:updateKotlinAbi rewrites the dumps)
./gradlew :checkLicense   # Dependency license gate (licensee) of every component
./gradlew :check -Pcheck.shard=2/4  # One shard of :check (see Sharding)
./gradlew :shardIntegrationTest     # Integration tests (of one shard with -Pcheck.shard; see Sharding)
./gradlew :mergeShardReports        # Aggregate test/coverage reports from shard results on disk; run separately from :check
./gradlew :test           # Run tests across all components
./gradlew :detekt         # Lint all components
./gradlew :publish        # Publish to GitHub Packages (requires `GitHubPackagesUsername`/`GitHubPackagesPassword`)
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
   - `gradle/plugins` — Local convention plugins (IDs `com.kelvsyc.internal.gradle-tools.*`), applied by the cores next to the shared ones (see Core Convention Plugins): `core` and `detekt` (every core) and `gradle-integration-test` (cores with integration tests)

2. **`cores/`** — Published Gradle plugins (group `com.kelvsyc.gradle`). Each is an independent included build. Types:
   - `*-extensions` — Gradle API utility libraries
   - `clients-base` — Base service for SDK client management (prerequisite for most AWS/GCP plugins)
   - `*-base` — Plugins for specific cloud services (Artifactory, AWS S3/SQS/SNS/SES/SecretsManager/CodeArtifact/IMDS, GCP Artifact Registry/Storage, Git)

3. **`aggregation/`** — Aggregates Dokka docs, JaCoCo coverage and test reports, and runs the compat tests. **`metadata/`** publishes the BOM and version catalog; both list every core's library and read the plugin IDs (marker constraints, catalog plugin aliases) from the cores' `plugins.register("<name>") { id = "..." }` declarations, failing if a registration does not have that shape.

### Shared Build Conventions

The catalog, platform BOM, semver versioning, repositories, toolchain resolver, and the Gradle-plugin kind, Dokka and publishing conventions come from [gradle-build-conventions](https://github.com/kelvSYC/gradle-build-conventions) (group `com.kelvsyc.internal`, published to GitHub Packages). Its settings plugin `com.kelvsyc.internal.settings` provides:

- the foojay toolchain resolver and the repositories (Maven Central, GitHub Packages for gradle-build-conventions and kotlin-tools, Gradle `libs-releases` for `org.gradle.experimental`)
- the `libs` version catalog (`com.kelvsyc.internal:catalog`) and the platform BOM (`libs.conventions.platform`), whose dependency versions are therefore not declared in this repository
- project versions from git tags (`com.kelvsyc.internal.semver`): past the latest tag the version is an implicit `-SNAPSHOT` of the next version, and uncommitted or untracked changes append `+dirty`. CI publishes snapshots with `-Dsemver.stage=snapshot -Dsemver.scope=patch`
- versionless resolution of plugin IDs in exactly the `com.kelvsyc.internal` namespace; the local plugins live in `com.kelvsyc.internal.gradle-tools.*`, which resolve from the included `gradle/plugins` build

`gradle/settings` applies it with `apply(plugin = "com.kelvsyc.internal.settings")` rather than a `plugins {}` block: generating a precompiled settings plugin's accessors applies its `plugins {}` block to a stand-in `Settings` after settings evaluation, where Gradle rejects the repositories the shared plugin declares.

The shared plugin owns the `libs` catalog, so gradle-tools' own entries (the kotlin-tools pins) live in `gradle/libs.versions.toml` and are exposed as the `local` catalog (`local.kotlin.tools.*`). Gradle itself imports `gradle/libs.versions.toml` under the default name `libs` in a build whose directory contains one (the repository root), which would clash with the shared catalog, so the root build renames that default import to `local`. Inside `gradle/settings` itself, before the shared plugin is applied, the same file is registered as `libs` to resolve the shared settings plugin. The shared platform does not manage kotlin-tools, so those entries carry an explicit `version.ref`.

The shared version is pinned once, as `gradle-build-conventions` in `gradle/libs.versions.toml`; the thin settings plugin depends on `com.kelvsyc.internal:settings-plugin` at that version, which in turn selects the catalog and platform versions. Dependabot bumps it (`.github/dependabot.yml` gives it the GitHub Packages registries of gradle-build-conventions and kotlin-tools, with the `DEPENDABOT_PACKAGES_TOKEN` Dependabot secret, a token with `read:packages`, as credentials). A manual bump is that one line.

A bump PR from Dependabot completes itself (`.github/workflows/conventions-bump.yml`): it moves the Gradle wrapper to the Gradle version the new release is built for (`.github/scripts/follow-conventions-gradle.sh`, reading the release's catalog and refreshing the wrapper's checksum), pushes that to the PR, and enables GitHub auto-merge, so the PR merges once the required checks pass. Dependabot's release-age cooldown exempts `com.kelvsyc.internal:*` and `com.kelvsyc.kotlin:*` (our own releases). kotlin-tools bumps arrive as ordinary Dependabot PRs and are not auto-merged, and Dependabot does not propose standalone Gradle wrapper updates: the wrapper follows the conventions. The push and the auto-merge use the `CONVENTIONS_TOKEN` personal access token, stored in both the Dependabot and Actions secrets (a run started by Dependabot sees only the former, one started by the token's own push only the latter); only those two steps get it, not the Gradle build. A push or merge made with `GITHUB_TOKEN` triggers no workflows, so neither CI on the wrapper change nor the snapshot publish after the merge would run.

GitHub Actions are pinned to a full commit SHA with the full version as a trailing comment (`uses: actions/checkout@<sha> # v7.0.1`), which lets Dependabot classify an update as minor or patch. `.github/workflows/actions-bump.yml` enables auto-merge on a Dependabot Actions PR when the update is minor or patch and every bumped action is from `actions/*`, `github/*`, `dependabot/*`, `gradle/actions` or `anthropics/claude-code-action`, so the PR merges once the required checks pass. Major bumps and other actions stay manual. It uses the same `CONVENTIONS_TOKEN`, which must be allowed to merge workflow-file changes (a classic token needs the `workflow` scope; a fine-grained token needs Workflows read/write).

Resolving the shared plugin requires GitHub Packages credentials even to configure the build: the `gpr.user`/`gpr.key` Gradle properties (for example in `~/.gradle/gradle.properties`), else `GITHUB_ACTOR`/`GITHUB_TOKEN`. CI jobs that run Gradle need `packages: read` permission and both variables set.

### Core Convention Plugins

Every core's `build.gradle.kts` applies, in this order:

```kotlin
plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-gradle-library") // or com.kelvsyc.internal.kotlin-gradle-plugin
    id("com.kelvsyc.internal.gradle-tools.core")
    id("com.kelvsyc.internal.gradle-tools.detekt")
    id("com.kelvsyc.internal.gradle-tools.gradle-integration-test") // only cores with integration tests
    id("com.kelvsyc.internal.github-publishing")
}
```

Shared (from gradle-build-conventions, resolved at the pinned version):

- `com.kelvsyc.internal.kotlin-gradle-library` — Kotlin JVM library compiled against the Gradle API; publishes a `maven` publication. `com.kelvsyc.internal.kotlin-gradle-plugin` — the same for Gradle plugins, with `kotlin-dsl` and `java-gradle-plugin` (`pluginMaven` plus a marker per plugin). Both apply the core's Gradle tier (see Gradle Compatibility Tiers; the tier is exposed read-only as `kelvsycGradle.tier`), JDK 21 with no release override, Kotlin 2.3 for every compilation but `main`, the stdlib exclusions, `org.gradle.plugin.api-version`, `verifyGradleLinkage`, the Dependency Analysis plugin, Kotest on the JUnit Platform and the mockk/ByteBuddy test JVM args. Neither sets the group: the gradle-tools settings plugin sets `com.kelvsyc.gradle` as every build's default.
- `com.kelvsyc.internal.dokka` — Dokka HTML (built by `assemble`) and a Dokka Javadoc jar, with JDK, Kotlin stdlib and GitHub source links derived from the `origin` remote.
- `com.kelvsyc.internal.github-publishing` — the `GitHubPackages` repository, its URL derived from the `origin` git remote. It publishes resolved dependency versions (`verifyPomDependencyVersions`, which `check` depends on, fails if a publication's generated POM has a dependency without a version — POM generation needs no artifacts, so this builds no jars or docs; `RequireDependencyVersions` makes module metadata generation, and so `publish`, fail likewise) and strips the internal platform BOM (`com.kelvsyc.internal:platform`) from the POM and Gradle module metadata.

Local (in `gradle/plugins`):

- `com.kelvsyc.internal.gradle-tools.core` — gradlex `jvm-dependency-conflict-resolution` (with `enforceSlf4JSimple()`); the Gradle API link (`https://docs.gradle.org/current/kotlin-dsl/gradle`) in the Dokka documentation; the compat repository (see Gradle Compatibility Tiers). Root `:check` does not run each core's `check`, only selected tasks (`verifyGradleLinkage`, `checkKotlinAbi`, `licensee`, `verifyPomDependencyVersions`, detekt and, through the aggregate reports, the tests), so a new per-core verification must be added to root `check` as well, next to `verifyGradleLinkage`, where it covers the cores of the shard (see Sharding) or, unsharded, every core.
- Public-API dumps (also from `com.kelvsyc.internal.gradle-tools.core`, via Kotlin's built-in ABI validation, `abiValidation()` on the main compilation): each core's public API is committed at `cores/<core>/api/<core>.api`. `checkKotlinAbi` (per core, and root `./gradlew :checkKotlinAbi`; wired into root `check` next to `verifyGradleLinkage`) fails when the API differs from the dump; after an intended API change run `./gradlew :<core>:updateKotlinAbi` (or root `:updateKotlinAbi` for every core) and commit the dump. The release automation diffs these dumps. `gradle-extensions`' `gradle99` source set is not dumped (`src/main` changes are caught by the main dump), so an API change only in `src/gradle99` needs its release label by hand.
- Dependency license gate (also from `com.kelvsyc.internal.gradle-tools.core`, via the Cash App `app.cash.licensee` plugin, script `com.kelvsyc.internal.gradle-tools.license-check`): each core's `licensee` task fails when a runtime dependency has a license outside the allowlist (Apache-2.0, MIT, BSD-2-Clause, BSD-3-Clause, EPL-2.0, CDDL-1.0/1.1, Unicode-3.0/Unicode-DFS-2016, MIT-0, UPL-1.0). Root `./gradlew :checkLicense` runs it for every component; root `check` runs it next to `checkKotlinAbi`. The allowlist and every override live in that plugin script: `allowUrl("<license url>") { because("<license, verified by hand: where>") }` for a license URL licensee cannot map, `allowDependency(group, artifact, version) { because(...) }` for an artifact without license metadata. Add an override only after checking the license by hand and only if it is on the allowlist; artifacts that are not shipped (Gradle-supplied, the internal platform BOM) use `ignoreDependencies`. `spotbugs-annotations` (LGPL-2.1, transitive of the AWS Secrets Manager caching client) is allowed for that artifact only; the first-party `com.kelvsyc.kotlin` kotlin-tools libraries are ignored, their license being decided in that repository.
- `com.kelvsyc.internal.gradle-tools.detekt` — detekt with `gradle/detekt.yml` (see JDK Constraints).
- `com.kelvsyc.internal.jacoco` (shared, from gradle-build-conventions) — Applies Gradle's `jacoco`, sets `toolVersion` to the shared catalog's `jacoco`, and, with the `java` plugin, makes `jacocoTestReport` depend on `test` with XML and HTML reports required. `aggregation/jacoco` applies it too, so the merged report uses the JaCoCo version that recorded the data. Tests record execution data (`build/jacoco/test.exec`) for the aggregate report. A core's own report is not part of `check`; build it with `./gradlew :<core>:jacocoTestReport`. The agent stays on the test tasks in every CI run, pull requests included, because switching it off would change the test tasks' inputs and break build-cache reuse between `main` and pull-request runs; only the reports are limited to `main` (see Sharding).
- `com.kelvsyc.internal.gradle-tools.gradle-integration-test` — the `integrationTest` source set and task (see Rule 3 below).

### Component Settings Pattern

Every component's `settings.gradle.kts`:
```kotlin
pluginManagement {
    includeBuild("../../gradle/settings")
    // Gradle resolves the included settings plugin's external dependencies with this build's plugin repositories.
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.github.com/kelvSYC/gradle-build-conventions") {
            name = "GitHubPackagesBuildConventions"
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

plugins { id("com.kelvsyc.internal.gradle-tools.settings") }
includeBuild("../clients-base")  // inter-core dependency, as needed
```
The `repositories` block is needed because the included settings plugin depends on the shared settings plugin from GitHub Packages, and Gradle resolves those dependencies with the applying build's plugin repositories. Keep the `pluginManagement { repositories { ... } }` block byte-identical in every settings file (root, aggregation, metadata, `gradle/plugins`, every core); only the `includeBuild` path differs. The `gradle/settings` build is the one that supplies the plugin and declares its own repositories instead (see its `settings.gradle.kts`).

### Testing

Tests use [Kotest](https://kotest.io/) with JUnit Platform. TestKit runs record no coverage. Versioning is driven by git tags via the shared semver settings plugin (`com.kelvsyc.internal.semver`, applied by the shared settings plugin).

### Sharding

`-Pcheck.shard=<index>/<count>` (1-based, e.g. `2/4`) makes root `check` cover one shard; any other value than `<index>/<count>` with `1 <= index <= count` fails the build. Without it, root `check` covers everything. Only root `check`, `shardIntegrationTest` and `shardAssignment` read the property.

- **The rule:** the units are every core, `aggregation/compat` and, with `-Pcheck.shard.integration=true`, the integration tests (`integrationTest`). Each unit has a weight, its cost in light-core equivalents: `heavyUnitWeights` in the root `build.gradle.kts` lists the few heavy units (`gradle-extensions`, `integrationTest`, `aggregation/compat`, `dokka-dash`), every other core weighs 1, so a new core is sharded automatically (cores are read from `cores/`). Greedy longest-processing-time assigns them: heaviest unit first (ties by name), each to the shard with the smallest weight sum so far (ties to the lowest index). `./gradlew :shardAssignment -Pcheck.shard=1/<count>` (with `-Pcheck.shard.integration=true` if wanted) prints every shard's units and weight sum.
- **Adjusting the weights:** the numbers are the unit's test time divided by the median light core's, rounded, estimated from CI. Revisit them only when a shard finishes minutes after the others (compare the shard jobs' durations): adjust the heavy unit's number, or add a core with heavy tests to the map.
- **Integration tests:** root `shardIntegrationTest` runs the integration tests of the cores with a `src/integrationTest` directory: every one without `-Pcheck.shard`, else on the shard that owns the `integrationTest` unit only (a no-op on the others). A sharded `shardIntegrationTest` without `-Pcheck.shard.integration=true` fails, because no shard owns the unit. Run it in the same invocation as the sharded `check` (`./gradlew :check :shardIntegrationTest -Pcheck.shard=<index>/<count> -Pcheck.shard.integration=true`): a separate invocation configures the build again, which on the shards that do not own the unit is all it does, and must pass the same properties, or its assignment differs. In one invocation, the integration tests run alongside the unit tests rather than after them, and, without `--continue`, a failing task stops the build before every integration test has run, as a failed `check` used to skip the separate integration-test step. Without `check.shard.integration` (as in the gradle-build-conventions canary, which runs no integration tests) the cores are balanced among themselves.
- **What a shard runs:** `test`, `detekt`, `verifyGradleLinkage`, `checkKotlinAbi`, `licensee` and `verifyPomDependencyVersions` of its cores, and `aggregation/compat`'s tests on the shard that owns it. The shards' union is the unsharded `check` without its aggregate test report: no core is in two shards.
- **Reports:** the aggregate test report (`testAggregateTestReport`) and `:jacoco` (`testCodeCoverageReport`) depend on every core's tests, so sharded `check` runs neither, and running them next to a sharded `check` still runs every core's tests. Root `mergeShardReports` builds equivalent reports from the results already on disk (for gradle-extensions the coverage report analyses its main, baseline classes, which its tests run against, where `testCodeCoverageReport` analyses its gradle99 variant), without depending on any test task: `aggregation/testing`'s `mergeShardTestReports` (from each core's `build/test-results/test/binary`, to `build/reports/tests/test/merged-shards`), `aggregation/jacoco`'s `mergeShardCoverageReport` (each core's `build/jacoco/test.exec` against its `build/classes/*/main`, to `build/reports/jacoco/mergeShardCoverageReport`) and `collectIntegrationTestReports`. `mergeShardTestReports` builds the test and integration-test reports alone; `mergeShardReports` adds the coverage report. It is meant for after the shards' results have been put in place; on a single machine, run the shards first and `mergeShardReports` in a separate invocation.
- **CI** (`.github/workflows/gradle-build.yml`): the `shard` job is a matrix of four shards, shown as `build (1/4)` to `build (4/4)`, each running `./gradlew :check :shardIntegrationTest -Pcheck.shard=<shard>/4 -Pcheck.shard.integration=true` (the Verify step), which runs the integration tests on the shard they are assigned to. Each shard uploads its test results (`shard-<n>` artifact) and, on pushes only, its execution data and main classes. The `build` job, the repository's required status check and what `docs` and `publish` need, runs after every shard (even a failed one), unpacks their results into place, runs `./gradlew :mergeShardTestReports` (on pushes, `:mergeShardReports`, which adds the coverage report), publishes the test results, uploads the `integration-test` artifact and, only if every shard passed, the `test-aggregate` artifact and, on pushes, the `jacoco-aggregate` artifact (coverage is informational: produced on `main` only, with no pull-request comment or minimum), and fails unless every shard passed and it received every shard's results (it counts the `shard-<n>` archives against the shard job's `shards` output, the matrix size). Shard artifacts are kept 7 days and re-uploaded with `overwrite`, so re-running failed shards within that window merges the re-run shards with the others' earlier results; later, the other shards' results have expired, the count fails and the whole workflow must be re-run. A core compiled by several shards (as a dependency) takes its classes from the shard that ran its tests, the classes its execution data was recorded against. Adding a shard is adding it to the matrix; the shard count comes from the matrix size.
- **Locally:** `./gradlew :check -Pcheck.shard=2/4` (add `--dry-run` to list a shard's tasks).

### JDK Constraints (do not change)

The Gradle daemon is pinned to JDK 21 (`gradle/gradle-daemon-jvm.properties`). **Do not change `toolchainVersion` to 25** — Detekt 1.23.x cannot parse JVM version "25.x". The `com.kelvsyc.internal.gradle-tools.detekt` plugin overrides `jvmTarget = "22"` and `jdkHome` to JDK 21 for detekt tasks.

`cores/` components target JDK 21. **Use LTS JDKs only; a core goes above JDK 21 only if it needs newer Java for its own reasons.** Lowering the target widens the range of Gradle versions and JDKs a core works on; raising it strands consumers.

The Kotlin version comes from the shared catalog (`kotlin`, owned by gradle-build-conventions; see Shared Build Conventions) and must equal the Gradle wrapper's embedded Kotlin version (see the [compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html#kotlin)). `gradle-kotlin-dsl-plugins` pins `kotlin-stdlib` strictly to that version, so a higher value fails dependency resolution and a lower one is silently overridden. The wrapper follows the conventions release, which carries the matching Kotlin version, so the two move together through the single `gradle-build-conventions` pin.

Tests run on JDK 21 via toolchain. The shared Gradle-plugin kinds wire the required mockk/ByteBuddy JVM args (`-XX:+EnableDynamicAgentLoading`, `-Dnet.bytebuddy.experimental=true`, `-javaagent:[byte-buddy-agent]`). **Do not remove these.**

### Gradle Compatibility Tiers

Each core has a minimum Gradle, set by `com.kelvsyc.gradle.minimumGradle` in the core's `gradle.properties` (absent means `8.14`, any other value than `8.14` or `9.0` fails the build) and defined by the shared Gradle-plugin kinds (`GradleTier` in gradle-build-conventions), so a new tier is a gradle-build-conventions release. The property is read per build, so it belongs in the core's own `gradle.properties`. There are two tiers:

| Tier | Cores | Compiled against | Kotlin `apiVersion`/`languageVersion` |
|------|-------|------------------|---------------------------------------|
| 8.14 (default) | 50 | `org.gradle.experimental:gradle-public-api:8.14` | 2.0 (Gradle 8.14 embeds Kotlin 2.0.21) |
| 9.0 | 15: the 13 `aws-*-kotlin-base`, `aws-kotlin-extensions`, `google-cloud-artifact-registry-base` | `gradle-public-api:9.0.0` | 2.2 |

- **The rule:** the units are every core, `aggregation/compat` and, with `-Pcheck.shard.integration=true`, the integration tests (`integrationTest`). Each unit has a weight, its cost in light-core equivalents: `heavyUnitWeights` in the root `build.gradle.kts` lists the few heavy units (`gradle-extensions`, `integrationTest`, `aggregation/compat`, `dokka-dash`), every other core weighs 1, so a new core is sharded automatically (cores are read from `cores/`). Greedy longest-processing-time assigns them: heaviest unit first (ties by name), each to the shard with the smallest weight sum so far (ties to the lowest index). `./gradlew :shardAssignment -Pcheck.shard=1/<count>` (with `-Pcheck.shard.integration=true` if wanted) prints every shard's units and weight sum.
- Kotlin language 2.0 is deprecated as of Kotlin 2.4. When a future Gradle ships a Kotlin that drops 2.0, the 8.14 tier must be revisited.
- The 9.0 tier's floor comes from third-party code: `kotlinx-coroutines` 1.11 and the AWS SDK need Kotlin stdlib classes first shipped in Gradle 9.0. These cores support build scripts from Gradle 9.0, but Kotlin-source build-logic (e.g. `kotlin-dsl` convention plugins) only from Gradle 9.4, because the AWS SDK ships Kotlin 2.4 metadata that the Kotlin 2.2 compiler in Gradle 9.0 to 9.3 cannot read. That Kotlin-source minimum follows the Kotlin version the AWS SDK is built with, so SDK upgrades can raise it.
- `gradle-extensions` additionally publishes a `gradle99` variant (Gradle 9.9+, Kotlin 2.4) with its existing deprecations.
- Each core publishes its tier as `org.gradle.plugin.api-version`, so an older Gradle rejects it at dependency resolution rather than failing at run time.
- Published dependencies exclude `kotlin-stdlib`, `-jdk7`, `-jdk8` and `kotlin-reflect`; Gradle supplies them.
- `verifyGradleLinkage` (a task in each core, run by its `check` and by root `./gradlew :check`) checks every reference from a core and every jar it ships against its tier's minimum Gradle API and that Gradle's Kotlin stdlib/reflect. The report is at `build/reports/gradle-linkage/report.txt`.
- `aggregation/compat` (`TierCompatibilitySpec`, run by root `:test`, and by sharded root `check` on the shard that owns it) publishes a few cores to `build/compat-repo` (each core's `publish<Name>PublicationToCompatRepository` task, from `com.kelvsyc.internal.gradle-tools.core`; the repository is not a publishing repository, so `publish` never writes to it) and runs real Gradle distributions via TestKit: consumer resolution (including Kotlin-source build-logic with `gradle-kotlin-dsl-plugins`), rejection of a 9.0-tier core on 8.14, and one runtime smoke test per tier. Root `:test` downloads Gradle 8.14, 9.0.0 and 9.4.1 the first time, so it needs network.
  - `TierCompatibilitySpec` is abstract: it holds the consumers and one registration function per test, and `versions/GradleVersionSpecs.kt` has one subclass per Gradle version (`Gradle814`, `Gradle900`, `Gradle941`, `CurrentGradle`). The test task sets `maxParallelForks = 4`, so Gradle runs each version in its own JVM (4 versions, 4 workers on CI) and their distribution download, daemon start and dependency resolution overlap. Gradle hands test classes to its test JVMs in turn, in the order it finds the class files, so the subclasses are the only classes in their package (`com.kelvsyc.gradle.compat.versions`): found one after another, each goes to a JVM of its own. A new Gradle version is a new subclass there and a larger `maxParallelForks`, but Gradle also caps the test JVMs at `max-workers` (4 on CI's 4-vCPU runners, unless `org.gradle.workers.max` says otherwise): with more versions than that, the extra versions share a JVM with another and run one after the other in it, and the package isolation only spreads the versions evenly.
  - Each spec gives TestKit its own directory, `<compat.testKitDir>/<gradle version>`, because TestKit directories must not be shared by concurrent builds. `compat.testKitDir` is the `COMPAT_TESTKIT_DIR` environment variable, else `aggregation/compat/build/compat-testkit`. It is a JVM argument, not a task input, so it never invalidates the test task. CI does not cache it: each version's `caches` (resolved dependencies, generated Kotlin DSL jars) total about 1.7 GB, too much for the repository's cache quota for the time saved. The Gradle distributions themselves go to the real Gradle user home's `wrapper/dists`, which `setup-gradle` already caches.
- `GradleVariantSelectionSpec` (`gradle-extensions`) and `DokkaDashGradleVersionSpec` (`dokka-dash`) test their cores across Gradle versions, in the same way (`gradle-extensions` resolves its core from `build/variant-test-repo`, which, like the compat repository, is not a publishing repository: its `publishMavenPublicationToVariantTestRepository` task is standalone, so `publish` never writes to it): each is abstract, with one subclass per Gradle version, including one for the running Gradle that has no tests when that version already has a subclass. The test task asks for one JVM per version (`maxParallelForks`: 5 for `gradle-extensions`, 4 for `dokka-dash`), and each version has its own TestKit directory under `build/variant-testkit` or `build/gradle-version-testkit` respectively. Gradle hands test classes to its test JVMs in turn, in the order it finds the class files, so the subclasses are the only classes in their package (`com.kelvsyc.gradle.variantselection`, `com.kelvsyc.gradle.plugins.gradleversions`): found one after another, they go to distinct JVMs. Gradle caps the test JVMs at `max-workers` (4 on CI's 4-vCPU runners, unless `org.gradle.workers.max` says otherwise): with more versions than that, the extra versions share a JVM with another and run one after the other in it, and the package isolation only spreads the versions evenly. On CI, `gradle-extensions`' five versions therefore run in four JVMs, two of them one after the other. A new Gradle version is a new subclass there (and a larger `maxParallelForks`).

## Releasing

Every component (each core, the BOM and the catalog) is released together under one version, the `vX.Y.Z` git tag (see Shared Build Conventions: a tagged, clean `HEAD` resolves the bare version). Releases go to GitHub Packages only. Every push to `main` still publishes `-SNAPSHOT`s (the `publish` job of `gradle-build.yml`).

**Labels.** A PR's release bump comes from its labels: `release:major`, `release:minor`, none for patch. The rule depends on the latest release tag's major version:

| Change | 0.x | ≥ 1.0 |
|---|---|---|
| API removed or changed (a removed or changed line in a `cores/*/api/*.api` dump) | `release:minor` | `release:major` |
| API added only | none (patch) | `release:minor` |
| Non-API break (minimum Gradle tier raised, defaults changed, task/extension/plugin ID renamed, API changed only in `gradle-extensions`' `src/gradle99`) | `release:minor` (by hand; the PR template's checklist) | `release:major` |
| Fixes, docs, build, dependencies | none (patch) | none (patch) |

While 0.x, `release:major` counts as minor; only the release workflow's `bump: major` input leaves 0.x. A higher label than required is allowed.

**API dumps.** The API rule reads the dumps at `cores/<core>/api/<core>.api` (see Core Convention Plugins). After an intended API change, run `./gradlew :updateKotlinAbi` (or `./gradlew :<core>:updateKotlinAbi`) and commit the dumps with the change.

**Label check.** `.github/workflows/release-label.yml` (job `release-label`, script `.github/scripts/release-label-check.sh`) fails a PR whose labels are below what its own API change (diffed from the merge base) requires, naming the dumps and the required label. It re-runs on labelling, so adding the label fixes it.

**Conventions bumps.** `conventions-bump.yml` labels a gradle-build-conventions bump PR `release:minor` when a library in the new release's platform or catalog moves across a breaking version (major changed, or minor changed while 0.x; `.github/scripts/conventions-moves.sh`), since such a move reaches the published POMs. Otherwise the PR stays unlabelled. Other Dependabot PRs are labelled by the maintainer at merge.

**Repository prerequisites (owner, once).**

- Create the labels `release:major` and `release:minor` (`gh label create release:major`, `gh label create release:minor`). They must exist before `conventions-bump.yml`'s labelling step runs or a PR can use them.
- Add the `release-label` check to the required status checks of the "Main Ruleset", next to `build`. Until then it is advisory; the release workflow applies the API rule again regardless.
- Enable GitHub private vulnerability reporting (repository Settings, Code security), which is the reporting route `SECURITY.md` names.

**Running a release.** Run the `Release` workflow (`.github/workflows/release.yml`, `workflow_dispatch`, from `main` only) with `bump` (`auto`, `patch`, `minor`, `major`) and `dry-run`. Run it with `dry-run: true` first and read the plan in the run summary.

1. It releases the commit `main` pointed at when dispatched, and fails unless that commit's `build` check (the required check of `gradle-build.yml`) concluded `success`.
2. `.github/scripts/release-plan.sh` computes the version from the latest `vX.Y.Z` tag: the largest bump of the labels of the PRs merged since (first-parent commits of `main`; labels are read at dispatch, so a label added after merge counts), raised to what the API change over that range requires (`raised=` in the plan). Commits with no PR (direct pushes) count as patch and are listed as `direct=`. A `bump` input other than `auto` overrides the result but fails if it is lower. No commits since the latest tag fails. `dry-run` stops here.
3. It tags `HEAD` locally (annotated), runs `./gradlew :publish`, and only after a successful publish pushes the tag and creates the GitHub Release with generated notes, categorised by `.github/release.yml`.

The first release after the release automation uses `bump: minor` (→ `v0.2.0`), since earlier PRs are unlabelled.

**Partial publish.** GitHub Packages refuses to republish a version, so a release that failed during `:publish` leaves some packages published at that version. Delete that version from every package that has it (repository Packages, or `gh api -X DELETE /user/packages/maven/<package>/versions/<id>`), delete the remote tag if it was pushed (`git push origin :refs/tags/vX.Y.Z`) and any GitHub Release, then re-run the workflow.

**Publish and tag succeeded, release creation failed.** If `:publish` and the tag push succeeded but `gh release create` failed, the packages are fine: do not delete them or the tag. Create the release by hand with `gh release create vX.Y.Z --verify-tag --generate-notes`.

**License overrides.** The license gate's `allowDependency(group, artifact, version)` overrides (see Core Convention Plugins) are pinned to one version: when that artifact is bumped, `:checkLicense` fails until the override is checked again by hand and updated to the new version.

## Critical Gotchas

### Detekt + JDK 25 = FAILS ⚠️

The Gradle daemon is pinned to JDK 21 (`gradle/gradle-daemon-jvm.properties`). **Do not change to 25.**

Detekt 1.23.x's bundled Kotlin compiler cannot parse JVM version "25.x" and throws `IllegalArgumentException`. Workaround is already in place in `com.kelvsyc.internal.gradle-tools.detekt` (`jvmTarget = "22"` and `jdkHome` overrides) — don't remove them.

### Test JVM Args (ByteBuddy / mockk) ⚠️

Tests run on JDK 21, and mockk/ByteBuddy requires these JVM args (already wired in the shared Gradle-plugin kinds):
```
-XX:+EnableDynamicAgentLoading
-Dnet.bytebuddy.experimental=true
-javaagent:[byte-buddy-agent jar path]
```

Do not remove these from the conventions.

### Publishing Requires Credentials ⚠️

`./gradlew :publish` requires credentials: the `GitHubPackagesUsername`/`GitHubPackagesPassword` Gradle properties (for example in `~/.gradle/gradle.properties`, or as `ORG_GRADLE_PROJECT_GitHubPackagesUsername`/`ORG_GRADLE_PROJECT_GitHubPackagesPassword` environment variables). The publishing repository reads them at execution, which keeps the publish tasks configuration-cache compatible. Without them, it fails. Resolving dependencies from GitHub Packages is separate and still uses the `gpr.user`/`gpr.key` Gradle properties, else `GITHUB_ACTOR`/`GITHUB_TOKEN` (see Building Requires GitHub Packages Credentials). The repository URL is derived from the `origin` git remote, so `origin` must point at the GitHub repository.

### Building Requires GitHub Packages Credentials ⚠️

The shared build conventions and kotlin-tools are resolved from GitHub Packages when the build is configured, so every Gradle invocation needs credentials with `read:packages` (see Shared Build Conventions).

### Component Settings Pattern ⚠️

Do not modify the `pluginManagement`/`plugins` blocks in each settings file (see Component Settings Pattern under Architecture) — they wire up the composite build, and the GitHub Packages `repositories` block must stay identical everywhere.

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

The cores with integration tests are those with a `src/integrationTest` directory: root `shardIntegrationTest` (run in CI's Verify step) and `aggregation/testing`'s report collection derive them, so there is no list to update. The root build fails at configuration if a core applies the convention without `src/integrationTest` sources, or has them without applying it.

## Quick Navigation

- **Architecture & detailed build hierarchy**: see Architecture above
- **Linting config**: `gradle/detekt.yml`
- **Source code structure**: `cores/*/src/main/kotlin/`
- **Tests**: Mirror `src/main` structure under `src/test/`
