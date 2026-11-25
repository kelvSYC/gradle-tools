# ant-bridge

A configuration-cache-safe bridge for legacy Ant usage in Gradle builds, beyond Gradle 10.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.ant-bridge")
}
```

The plugin creates the `antBridge` dependency scope (Ant `1.10.18` as a preferred version, plus any antlibs you add),
the `antBridgeClasspath` resolvable configuration, the `antBridge` extension and the `antBridgeRuntime` shared build
service, which caches one isolated Ant class loader per Ant classpath for the build.

## Loading Ant properties: `AntProperties`

A `ValueSource<Map<String, String>, AntProperties.Parameters>` that loads properties as Ant's `property` task does, in
a fresh Ant project and an isolated class loader (Ant never goes on the build script classpath).

- Seeds are set first, then each source loads in declaration order. As in Ant, the first value set for a key wins, so
  seeds override the sources, and an earlier source wins over a later one. `${...}` references expand within and
  across sources.
- The result holds only the keys the seeds and sources name, each with Ant's effective value: a key already set as a
  system property reports the system property's value. Other system properties and Ant built-ins (`basedir`,
  `ant.*`) are excluded.
- `basedir` is the project directory, as with Gradle's built-in `ant`.
- A missing file contributes nothing and is logged at info. Files are read as ISO-8859-1 with `\uXXXX` escapes.
- Files, URLs and the environment are read only when the value is obtained, so changing them invalidates the
  configuration cache.

### Sources (`AntPropertiesSpec`)

| Function | Ant equivalent |
|---|---|
| `file(path)`, `file(path) { prefix.set("p"); prefixValues.set(true) }` | `<property file="..." prefix="..." prefixValues="..."/>`; `path` resolves against the project directory |
| `resource(name, classpath)`, `resource(name, classpath) { prefix.set("p") }` | `<property resource="..." classpath="..." prefix="..."/>` |
| `url(url)`, `url(url) { prefix.set("p") }` | `<property url="..." prefix="..."/>` |
| `environment(prefix)` | `<property environment="..."/>` |
| `seed(key, value)` | a property set before loading (overrides the sources) |

### Parameters (when using `providers.of` directly)

| Parameter | Type | Description |
|---|---|---|
| `sources` | `ListProperty<AntPropertySource>` | `File`, `Resource`, `Url` or `Environment`, loaded in order |
| `seeds` | `MapProperty<String, String>` | Properties set before any source |
| `baseDirectory` | `DirectoryProperty` | Ant's `basedir`, used by `${basedir}` references; the spec sets the project directory |
| `antClasspath` | `ConfigurableFileCollection` | The Ant classpath |
| `runtime` | `Property<AntRuntimeService>` | The `antBridgeRuntime` shared service |

### Example

```kotlin
// before: ant.property(file: "build.properties"); version = ant.properties["app.version"]
val legacy = antBridge.properties { file("build.properties") }
version = legacy.map { it.getValue("app.version") }.get()

// before: ant.property(environment: "env"); ant.properties["env.HOME"]
val home = antBridge.properties { environment("env") }.antProperty("env.HOME")

// before: ant.property(name: "base", value: "/srv"); ant.property(file: "build.properties")
val overridden = antBridge.properties {
    seed("base", "/srv")
    file("build.properties")
}

// before: ant.property(file: "build.properties", prefix: "cfg"); ant.properties["cfg.dir"]
val dir = antBridge.properties { file("build.properties") { prefix.set("cfg") } }.antProperty("cfg.dir")
```

`antProperty(key)` returns a `Provider<String>` that is absent when the key is not defined.

## Typed tasks (`com.kelvsyc.gradle.ant.tasks`)

Each extends `AbstractAntTask` (`antClasspath`, `isolation`; both wired by the plugin, isolation defaults to `CLASSLOADER`), declares its inputs and outputs, and never modifies an input in place.

| Task | Ant task | Inputs | Output | Cached |
|------|----------|--------|--------|--------|
| `AntReplaceRegexp` | `replaceregexp` | `sourceDirectory`, `includes`, `excludes`, `match`, `replace`, `flags`, `byLine`, `encoding` | `outputDirectory` (a filtered copy, then replaced in place there) | yes |
| `AntXslt` | `xslt` | `sourceDirectory`, `includes`, `excludes`, `stylesheet`, `parameters`, `extension` | `outputDirectory` | yes |
| `AntChecksum` | `checksum` | `files`, `algorithm` (`SHA-256`), `fileExtension` (`.sha256`) | `outputDirectory` | yes |
| `AntSignJar` | `signjar` | `jars`, `keystore`, `alias`, `storeType`, `sigAlg`, `digestAlg`, `tsaUrl` | `outputDirectory` | no |
| `AntJUnitReport` | `junitreport` | `resultsDirectory`, `includes` (`TEST-*.xml`), `format` (`noframes`) | `outputDirectory` | yes |

`AntSignJar` takes `storePassword` and `keyPassword` as `Property<CredentialReference>`; they are resolved at execution and are not task inputs. `AntJUnitReport` needs `org.apache.ant:ant-junit`, which the plugin adds by default.

```kotlin
tasks.register<AntChecksum>("checksums") {
    files.from(tasks.jar)
    outputDirectory.set(layout.buildDirectory.dir("checksums"))
}
```
