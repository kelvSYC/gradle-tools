# Dokka Dash Docset Plugin

A Gradle plugin that packages the HTML output of a [Dokka](https://kotl.in/dokka) documentation build into a
[Dash](https://kapeli.com/dash) docset.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.dokka-dash")
}
```

## Task: `GenerateDashDocset`

Assembles a `.docset` bundle from an existing Dokka HTML output directory. The bundle contains the full HTML
tree, an `Info.plist`, and a SQLite search index (`docSet.dsidx`) populated from Dokka's `navigation.json`.

### Properties

| Property | Type | Required | Description |
|---|---|---|---|
| `dokkaOutputDirectory` | `DirectoryProperty` | Yes | Root directory of the Dokka HTML output to package |
| `docsetName` | `Property<String>` | Yes | Base name of the `.docset` bundle and its display name in Dash |
| `bundleIdentifier` | `Property<String>` | Yes | Reverse-DNS identifier (e.g. `com.example.mylibrary`); populates `CFBundleIdentifier` and lowercased `DocSetPlatformFamily` |
| `indexPage` | `Property<String>` | No | Page Dash opens when selecting the docset; defaults to `index.html` |
| `outputDirectory` | `DirectoryProperty` | Yes | Parent directory that will contain the `.docset` bundle; no `.docset` suffix needed |

### Read-only outputs

| Property | Type | Description |
|---|---|---|
| `docsetDirectory` | `Provider<Directory>` | The `.docset` bundle directory, computed as `<outputDirectory>/<docsetName>.docset`; use this to wire the bundle as an input to downstream tasks |

### Example

Wire the task directly to Dokka's output so that building the docset automatically triggers documentation
generation:

```kotlin
import com.kelvsyc.gradle.dokka.GenerateDashDocset
import org.jetbrains.dokka.gradle.tasks.DokkaGeneratePublicationTask

plugins {
    id("org.jetbrains.dokka") version "2.2.0"
    id("com.kelvsyc.gradle.dokka-dash")
}

val generateDashDocset = tasks.register<GenerateDashDocset>("generateDashDocset") {
    val dokkaTask = tasks.named<DokkaGeneratePublicationTask>("dokkaGeneratePublicationHtml")
    dokkaOutputDirectory.set(dokkaTask.flatMap { it.outputDirectory })
    docsetName.set("MyLibrary")
    bundleIdentifier.set("com.example.mylibrary")
    outputDirectory.set(layout.buildDirectory.dir("dash"))
}
```

The produced bundle is written to `build/dash/MyLibrary.docset`. Use `docsetDirectory` to wire it
to a downstream task without hardcoding the path:

```kotlin
tasks.register<Zip>("packageDashDocset") {
    from(generateDashDocset.flatMap { it.docsetDirectory })
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    archiveFileName.set("MyLibrary.docset.zip")
}
```

## Worker Dependencies

The plugin creates a `dokkaDashWorkerClasspath` dependency scope pre-populated with the version of
`sqlite-jdbc` it was built against. To override the version or add extra JARs to the worker process,
declare dependencies against that scope:

```kotlin
dependencies {
    dokkaDashWorkerClasspath("org.xerial:sqlite-jdbc:3.49.1.0")
}
```

## See Also

- [Dokka](https://kotl.in/dokka) — Kotlin documentation engine whose HTML output this plugin consumes
- [Dash docset format](https://kapeli.com/docsets) — Specification for the `.docset` bundle layout and SQLite index schema
