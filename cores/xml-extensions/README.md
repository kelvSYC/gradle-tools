# xml-extensions

Gradle integration layer for XML parsing, built on top of
[kotlin-tools xml-extensions](https://github.com/kelvSYC/kotlin-tools) which provides the
core XML tree API, StAX-based parser, and XPath query engine.

This module provides Gradle-specific utilities: `ValueSource` implementations for reading XML
files, `Provider` extensions for lazy XML parsing, and migration helpers for codebases
transitioning from Groovy's `XmlSlurper`.

---

## ValueSources

### `XmlValueSource`

Reads an XML file and provides the parsed `XmlElement` tree.

```kotlin
val xmlProvider = providers.xmlFile(layout.projectDirectory.file("pom.xml"))
val artifactId = xmlProvider.map { it.element("artifactId")?.stringValue }
```

### `XPathValueSource`

Extracts a single string value from an XML file using an XPath expression.

```kotlin
val version = providers.xpath(
    layout.projectDirectory.file("pom.xml"),
    "version"
)
```

**Note:** The underlying `XPath` implementation in kotlin-tools is a deliberately partial
implementation of XPath 1.0. It supports child navigation (`name`, `prefix:name`), descendant
search (`//`), wildcards (`*`), attribute access (`@attr`), node tests (`text()`, `node()`), and
predicates (`[n]`, `[@attr='value']`, `[child]`). It does **not** support axes syntax, functions,
arithmetic operators, union (`|`), or parent navigation — these are intentionally excluded in
favour of expressing complex logic in Kotlin.

Both ValueSources return an absent provider if the file is missing, the XML is malformed, or
(for `XPathValueSource`) the path matches zero or multiple nodes.

## Provider Extensions

### `Provider<String>.parseXml()`

Lazily parses a string provider's value as an `XmlElement` tree.

```kotlin
val xmlContent = providers.fileContents(layout.projectDirectory.file("config.xml")).asText
val parsed = xmlContent.parseXml()
```

## ProviderFactory Extensions

Convenience functions on `ProviderFactory` for creating ValueSource-backed providers:

- `xmlFile(file)` / `xmlFile(fileProvider)` — returns `Provider<XmlElement>`
- `xpath(file, path)` — returns `Provider<String>` with all combinations of
  `RegularFile`/`Provider<RegularFile>` and `String`/`Provider<String>`

## Groovy Migration Helpers

The `com.kelvsyc.gradle.xml.groovy` package provides operator extensions on `XmlElement` that
mimic Groovy's `XmlSlurper` GPathResult navigation. These are intended as a **temporary migration
aid** — once the migration is complete, replace usages with the direct accessors from kotlin-tools
(`element`, `elements`, `attr`, `query`, etc.) and remove the import.

```kotlin
import com.kelvsyc.gradle.xml.groovy.*

val pom = providers.xmlFile(layout.projectDirectory.file("pom.xml")).get()

// GPathResult-style chained navigation
val groupId = pom["dependencies"]?.get("dependency")?.get("groupId")?.stringValue
```

- `operator fun XmlElement.get(name: String): XmlElement?` — navigates to the first child element
  with the given name; returns `null` if no such child exists
- `operator fun XmlElement.get(index: Int): XmlElement?` — navigates to the child element at the
  given position; returns `null` if the index is out of bounds
