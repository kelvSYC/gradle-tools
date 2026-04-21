# AGENTS.md

Critical gotchas for OpenCode agents working in this Gradle composite build.

## Build Commands

- Root: `./gradlew :build` (aggregate), `./gradlew :test` (aggregate)
- Single component: `cd cores/foo-bar && ../gradlew :build` (not from root)

## Detekt + JDK 25 = FAILS

The Gradle daemon runs on JDK 21 (via `gradle/gradle-daemon-jvm.properties`). **Do not change this to 25**. Detekt 1.23.x's bundled Kotlin compiler cannot parse JVM version "25.x" and throws `IllegalArgumentException`.

Workaround already in place: convention plugins override `jvmTarget = "22"` and `jdkHome` to JDK 21 for Detekt tasks.

## Test JVM Args (ByteBuddy / mockk)

Tests run on JDK 25 but mockk uses ByteBuddy which requires these JVM args:
```
-XX:+EnableDynamicAgentLoading
-Dnet.bytebuddy.experimental=true
```
Plus pre-attach the byte-buddy-agent at startup. These are already wired in convention plugins — don't remove them.

## Publishing

Requires `GITHUB_ACTOR` and `GITHUB_TOKEN` env vars. Without these, `./gradlew :publish` fails silently or with obscure errors.

## Component Settings Pattern

Every component has its own `settings.gradle.kts` that includes:
```kotlin
pluginManagement { includeBuild("../../gradle/settings") }
plugins { id("com.kelvsyc.internal") }
```
Don't modify these — they wire up the composite build correctly.

## Entry Points

- Core plugins: `cores/*/src/main/kotlin/`
- Extensions: `extensions/*/src/main/kotlin/`
- Tests mirror `src/main` structure under `src/test/`

## References

- Full build docs: `CLAUDE.md`
- Detekt config: `gradle/detekt.yml`