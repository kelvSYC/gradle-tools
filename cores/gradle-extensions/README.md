# kelvSYC Gradle Tools - Gradle Extensions

This library contains extensions and other library tools that can be used in developing Gradle plugins with Kotlin.

## Usage

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:gradle-extensions")
}
```

## Components

The vast majority of the Gradle Extensions are Kotlin extensions to the Gradle API, and is provided as syntactic sugar.

* `Provider.asAbsolutePath` is a simple transformation that returns a `FileSystemLocation`'s absolute path.
  `Provider.asPath` is similar, but returns a `Path`.
* `Provider.asBoolean` converts a string `Provider` to a boolean `Provider`. Similarly, `Provider.asInt` converts a
  string `Provider` to an integer `Provider`.
* `Provider.asMap` is a simple transformation converting a `Properties` object to a string map.
* `Provider.dir()` and `Provider.file()` are extensions that transform directories like they do for `DirectoryProperty`.
* `Provider.filterNotBlank()` is an extension that filters out blank strings.
* `Provider.mapElements` is an extension that maps the elements of a collection. `Provider.mapElementsNotNull` uses
  `mapNotNull()` on the underlying collection instead.
* `Provider.mapKt()` is an extension that can be used to work around an [issue in Gradle](https://github.com/gradle/gradle/issues/12388)
  that, when working with Kotlin, `map()` requires that the transformed value be non-null, despite the fact that the
  transformed value is nullable. `Provider.flatMapKt()` works similarly, but for `flatMap()`.
* `Provider.orElseEmpty` is an extension that allows a `Provider` to fall back to an empty collection. A similar
  function exist for `Provider`s of strings.
* `ProviderFactory.ofNullable` is a simple `Provider` that returns a constant value. `ProviderFactory.absent` returns
  an always-absent `Provider`
* `ProviderFactory.propertiesFile()` creates `Provider` instances from properties files.
