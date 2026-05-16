# Karakum Core

A Gradle plugin that integrates [Karakum](https://github.com/karakum-team/karakum) TypeScript `.d.ts` → Kotlin external-declaration generation into the build lifecycle.

When applied alongside `kotlin("js")` or `kotlin("multiplatform")`, the plugin automatically registers a generation task and wires its output into the appropriate Kotlin source set.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.karakum-core")
}
```

## Extension: `karakum`

The plugin registers a `karakum` extension for project-level configuration.

### `karakumVersion`

Pins the npm package version used in npx-based invocations. Analogous to `toolVersion` in JVM code-quality plugins.

```kotlin
karakum {
    karakumVersion.set("1.2.3")   // produces: npx karakum@1.2.3
}
```

When unset, the latest published version is used. Has no effect when `useSystem()` or `useNodeModules()` is active.

### Invocation presets

| Method | Invoked as | Notes |
|--------|-----------|-------|
| `useNpx()` | `npx karakum[@ver]` | Default (global); bootstrapping-friendly, hits npm on each run |
| `useNpx(npxBinary)` | `<binary> karakum[@ver]` | Used automatically when KMP Node.js plugin is detected |
| `useNodeModules()` | `node_modules/.bin/karakum` | Reproducible; requires a `karakum` entry in `package.json` |
| `useNodeModules(nodeBinary)` | `<binary> node_modules/.bin/karakum` | Reproducible with explicit Node.js binary |
| `useSystem()` | `karakum` (PATH lookup) | No Node.js management; requires a global install |

The plugin default is `useNpx()` (system `npx`). When a `kotlin("js")` or `kotlin("multiplatform")` project also applies the Kotlin Node.js plugin, the default is automatically upgraded to route through the KMP-managed `npx` binary. Call a preset method explicitly in the `karakum {}` block to override:

```kotlin
karakum {
    useNodeModules()   // reproducible builds once karakum is in package.json
}
```

## Auto-Registered Tasks

When a Kotlin plugin is detected, `KarakumPlugin` registers a generation task and wires its output into the Kotlin source set.

### `generateKotlinExternals` (Kotlin/JS, single-platform)

Registered when `kotlin("js")` is applied. Outputs to `build/generated/karakum/main`; wired into the `main` source set automatically.

### `generateKotlinJsExternals` (Kotlin Multiplatform)

Registered when `kotlin("multiplatform")` is applied. Outputs to `build/generated/karakum/jsMain`; wired into the `jsMain` source set when a JS target is configured.

Configure either auto-registered task by name:

```kotlin
tasks.named<KarakumTask>("generateKotlinExternals") {
    // Direct mode: list .d.ts files or directories
    inputFiles.from("node_modules/@types/my-lib")

    // Config-file mode: delegate all input routing to a JSON config
    // configFile.set(layout.projectDirectory.file("karakum.config.json"))
}
```

## `KarakumTask`

`KarakumTask` is a `@CacheableTask`. Register additional instances manually when more than one generation step is needed:

```kotlin
tasks.register<KarakumTask>("generateExtraExternals") {
    inputFiles.from("src/extra-types")
    outputDirectory.set(layout.buildDirectory.dir("generated/extra-externals"))
}
```

### Properties

| Property | Type | Description |
|---|---|---|
| `inputFiles` | `ConfigurableFileCollection` | `.d.ts` files or directories. Ignored when `configFile` is set. |
| `configFile` | `RegularFileProperty` | Path to a `karakum.config.json`. When set, Karakum is invoked with `--config` and input/output flags are omitted. |
| `outputDirectory` | `DirectoryProperty` | Directory for generated Kotlin sources. Wired into source sets automatically for the plugin-registered tasks. |
| `karakumCommand` | `ListProperty<String>` | Full invocation command. Supplied by the `karakum` extension convention; override here for per-task customisation. |

## `RunKarakumAction`

Each `KarakumTask` delegates to `RunKarakumAction` via `WorkerExecutor.noIsolation()`. Use it directly when composing Karakum invocation inside a custom task:

```kotlin
abstract class MyTask @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {
    @TaskAction
    fun run() {
        workers.noIsolation().submit(RunKarakumAction::class) {
            karakumCommand.set(listOf("npx", "karakum@1.2.3"))
            inputFiles.from(layout.projectDirectory.dir("src/types"))
            outputDirectory.set(layout.buildDirectory.get().asFile.resolve("generated/karakum").absolutePath)
        }
    }
}
```

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `karakumCommand` | `ListProperty<String>` | Full command tokens. First token is the executable; remaining tokens are prepended to Karakum's own arguments (e.g. `["npx", "karakum@1.2.3"]`). |
| `inputFiles` | `ConfigurableFileCollection` | TypeScript declaration files or directories. Ignored when `configFile` is set. |
| `configFile` | `Property<String>` | Absolute path to a `karakum.config.json`. When present, Karakum is invoked with `--config` and input/output flags are not passed. |
| `outputDirectory` | `Property<String>` | Absolute path to the output directory. Ignored when `configFile` is set. |

## See Also

- [Karakum repository](https://github.com/karakum-team/karakum) — TypeScript-to-Kotlin external declaration generator
- [Kotlin/JS overview](https://kotlinlang.org/docs/js-overview.html)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
