# moshi-extensions

Gradle integration layer for [Moshi](https://github.com/square/moshi), built on top of
[kotlin-tools moshi-extensions](https://github.com/kelvSYC/kotlin-tools) which provides the
core JSON tree API, JsonPath queries, and streaming DSL extensions.

This module provides Gradle-specific utilities: `ValueSource` implementations for reading JSON
files, `Provider` extensions for lazy JSON parsing, and Moshi adapters for common JDK types used
in Gradle builds.

---

## ValueSources

### `JsonValueSource`

Reads a JSON file and provides the parsed `JsonValue` tree.

```kotlin
val jsonProvider = providers.jsonFile(layout.projectDirectory.file("config.json"))
val name = jsonProvider.map { it.stringAt("project", "name") }
```

### `JsonPathValueSource`

Extracts a single scalar value from a JSON file using a JsonPath expression, returned as a string.

```kotlin
val version = providers.jsonPath(
    layout.projectDirectory.file("package.json"),
    "$.version"
)
```

**Note:** The underlying `JsonPath` implementation in kotlin-tools is a partial implementation of
[RFC 9535](https://www.rfc-editor.org/rfc/rfc9535.html). It supports root (`$`), child access
(`.key`, `['key']`, `[0]`), wildcards (`.*`, `[*]`), and recursive descent (`..key`), but does
**not** support array slices (`[start:end:step]`), filter expressions (`[?expression]`), or
function extensions.

Both ValueSources return an absent provider if the file is missing, the JSON is malformed, or
(for `JsonPathValueSource`) the path matches zero or multiple nodes.

## Provider Extensions

### `Provider<String>.parseJson()`

Lazily parses a string provider's value as a `JsonValue` tree.

```kotlin
val jsonContent = providers.fileContents(layout.projectDirectory.file("data.json")).asText
val parsed = jsonContent.parseJson()
```

## ProviderFactory Extensions

Convenience functions on `ProviderFactory` for creating ValueSource-backed providers:

- `jsonFile(file)` / `jsonFile(fileProvider)` — returns `Provider<JsonValue>`
- `jsonPath(file, path)` — returns `Provider<String>` with all combinations of
  `RegularFile`/`Provider<RegularFile>` and `String`/`Provider<String>`

## Groovy Migration Helpers

The `com.kelvsyc.gradle.moshi.groovy` package provides operator extensions on `JsonValue` that
mimic Groovy's `JsonSlurper` dynamic property access. These are intended as a **temporary migration
aid** for codebases transitioning from Groovy to Kotlin — once the migration is complete, replace
usages with the typed accessors from kotlin-tools (`stringAt`, `objectAt`, `at`, etc.) and remove
the import.

```kotlin
import com.kelvsyc.gradle.moshi.groovy.*

val json = """{"users": [{"name": "Alice"}]}""".parseJson()

// Groovy-style chained navigation
val name = json["users"]?.get(0)?.get("name")?.asString()  // "Alice"
```

- `operator fun JsonValue.get(key: String): JsonValue?` — navigates by object key; returns `null`
  on type mismatch or missing key
- `operator fun JsonValue.get(index: Int): JsonValue?` — navigates by array index; returns `null`
  on type mismatch or out-of-bounds index

## Gradle Type Adapters

`Moshi.Builder.addGradleTypeAdapters()` registers adapters for common types:

- `UriAdapter` — serializes `java.net.URI` as a string
- `FileAdapter` — serializes `java.io.File` as an absolute path string
