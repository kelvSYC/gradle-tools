# pkl-extensions

Gradle integration layer for [Pkl](https://pkl-lang.org/), Apple's configuration language.
Unlike the JSON and XML extensions which wrap kotlin-tools libraries, this module depends
directly on Pkl's native Kotlin API (`org.pkl-lang:pkl-core`).

This module provides Gradle-specific utilities: `ValueSource` implementations for evaluating Pkl
files and extracting configuration values, `Provider` extensions for lazy Pkl evaluation, and
convenience `ProviderFactory` extensions.

---

## ValueSources

### `PklValueSource`

Evaluates a Pkl file and provides the resulting `PModule`.

```kotlin
val config = providers.pklFile(layout.projectDirectory.file("config.pkl"))
val appName = config.map { it.properties["name"] as String }
```

The evaluator is preconfigured with default security settings, allowing local file imports
and the Pkl standard library. Evaluated modules remain valid after the evaluator closes.

### `PklPathValueSource`

Extracts a single string value from a Pkl file using a dot-notation path expression.

```kotlin
val host = providers.pklPath(
    layout.projectDirectory.file("config.pkl"),
    "database.host"
)
```

**Path expression syntax:** A dot-separated list of property names starting from the module root.
Each segment navigates into a nested object property. For example, given:

```pkl
database = new {
    host = "localhost"
    port = 5432
}
```

- `"database.host"` resolves to `"localhost"`
- `"database.port"` resolves to `"5432"` (numbers are coerced to strings)
- `"database"` resolves to absent (non-scalar value)
- `"database.missing"` resolves to absent (property not found)

Scalar values are coerced to strings: strings are returned directly, numbers (`Int`, `Float`)
and booleans are converted via `toString()`. Non-scalar values (objects, listings, mappings, null)
result in an absent provider.

Both ValueSources return an absent provider if the file is missing, the Pkl content is malformed,
or (for `PklPathValueSource`) the path does not resolve to a scalar value.

## Provider Extensions

### `Provider<String>.parsePkl()`

Lazily evaluates a string provider's value as a Pkl `PModule`.

```kotlin
val pklContent = providers.provider { "name = \"my-app\"" }
val module = pklContent.parsePkl()
```

**Note:** Each call creates a fresh evaluator. Since text-based modules have no base path,
relative imports within the Pkl content will not resolve.

## ProviderFactory Extensions

Convenience functions on `ProviderFactory` for creating ValueSource-backed providers:

- `pklFile(file)` / `pklFile(fileProvider)` — returns `Provider<PModule>`
- `pklPath(file, path)` — returns `Provider<String>` with all combinations of
  `RegularFile`/`Provider<RegularFile>` and `String`/`Provider<String>`
