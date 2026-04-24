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

## Detekt Formatting Rules (avoid failures before validation)

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