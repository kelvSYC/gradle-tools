# AGENTS.md

Critical gotchas and quick reference for automated agents (OpenCode, etc.) working in this Gradle composite build. For comprehensive architecture and details, see `CLAUDE.md`.

## Requirements for All Agents

- **Tests and detekt must pass.** All code changes must pass `./gradlew :test` and `./gradlew :detekt` before completing a task. Breaking either check is not acceptable.
- **README must stay current.** Any change that adds or significantly modifies a public-facing component — including Gradle tasks, `WorkAction` implementations, `ValueSource` implementations, extensions, or plugin behaviour — must include a corresponding update to the component's `README.md`.
- **Be concise.** Avoid unnecessary explanation or narrative.
- **No unsolicited explanations.** Only explain what you're doing if explicitly asked.

## Build Commands

Root commands (aggregate across all components):
```bash
./gradlew :build          # Full build + tests + coverage
./gradlew :test           # Tests only
./gradlew :detekt         # Lint only
./gradlew :publish        # Publish to GitHub Packages
```

Single component (included build form, from repository root):
```bash
./gradlew :artifactory-base:build
./gradlew :artifactory-base:detekt
```

## Critical Gotchas

### Detekt + JDK 25 = FAILS ⚠️

The Gradle daemon is pinned to JDK 21 (`gradle/gradle-daemon-jvm.properties`). **Do not change to 25.**

Detekt 1.23.x's bundled Kotlin compiler cannot parse JVM version "25.x" and throws `IllegalArgumentException`. Workaround is already in place in convention plugins (`jvmTarget = "22"` and `jdkHome` overrides) — don't remove them.

### Test JVM Args (ByteBuddy / mockk) ⚠️

Tests run on JDK 25 but mockk/ByteBuddy requires these JVM args (already wired in convention plugins):
```
-XX:+EnableDynamicAgentLoading
-Dnet.bytebuddy.experimental=true
-javaagent:[byte-buddy-agent jar path]
```

Do not remove these from convention plugins.

### Publishing Requires Env Vars ⚠️

`./gradlew :publish` requires `GITHUB_ACTOR` and `GITHUB_TOKEN`. Without them, it fails silently or with obscure errors.

### Component Settings Pattern ⚠️

Every component's `settings.gradle.kts` includes:
```kotlin
pluginManagement { includeBuild("../../gradle/settings") }
plugins { id("com.kelvsyc.internal") }
includeBuild("../clients-base")  // if needed
```

Do not modify these — they wire up the composite build correctly.

## Quick Navigation

- **Architecture & detailed build hierarchy**: See `CLAUDE.md`
- **Linting config**: `gradle/detekt.yml`
- **Source code structure**: `cores/*/src/main/kotlin/`, `extensions/*/src/main/kotlin/`
- **Tests**: Mirror `src/main` structure under `src/test/`