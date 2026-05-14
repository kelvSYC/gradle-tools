# pkl-extensions

Gradle integration layer for [Pkl](https://pkl-lang.org/), Apple's configuration language.
Unlike the JSON and XML extensions which wrap kotlin-tools libraries, this module depends
directly on Pkl's native Kotlin API (`org.pkl-lang:pkl-core`).

This module provides `ValueSource` implementations for evaluating Pkl files and extracting
configuration values, and convenience `ProviderFactory` extensions.

---

## ValueSources

### `AbstractPklValueSource` (abstract base)

Abstract base class for `ValueSource` implementations that evaluate a Pkl file. Subclasses
implement `doObtain(PModule)` to transform the evaluated module into a value of type `T`.

> **Configuration cache compatibility is incomplete.** For a subclass to be compatible with
> Gradle's configuration cache, the return type `T` must implement `java.io.Serializable`.
> `PModule` itself does not, so subclasses that return `PModule` directly will fail under
> `--configuration-cache`. Use `PklPathValueSource` for a config-cache-safe alternative.

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

Returns an absent provider if the file is missing, the Pkl content is malformed, or the path
does not resolve to a scalar value.

## ProviderFactory Extensions

Convenience functions on `ProviderFactory` for creating ValueSource-backed providers:

- `pklPath(file, path)` — returns `Provider<String>` with all combinations of
  `RegularFile`/`Provider<RegularFile>` and `String`/`Provider<String>`
