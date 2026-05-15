# snakeyaml-extensions

Gradle `ValueSource` implementations and `Provider` extensions for YAML parsing, backed by
[kotlin-tools `snakeyaml-extensions`](https://github.com/kelvSYC/kotlin-tools).

## Usage

### Parsing a YAML file

Use `ProviderFactory.yamlFile()` to parse a YAML file into a `Provider<YamlValue>` tree.
The provider is evaluated lazily and is configuration-cache safe.

```kotlin
val config: Provider<YamlValue> = providers.yamlFile(layout.projectDirectory.file("config.yaml"))
```

### Extracting typed values

Use the typed extension functions on `Provider<YamlValue>` to extract scalars and navigate nested
structures. Each extension takes vararg path segments — one per level of nesting — and returns
absent when the path does not resolve or the value is the wrong type.

```kotlin
val host: Provider<String> = config.stringAt("server", "host")
val port: Provider<Int>    = config.intAt("server", "port")
val debug: Provider<Boolean> = config.booleanAt("server", "debug")
val ratio: Provider<Double> = config.doubleAt("server", "ratio")
```

Sequence elements are addressed by their index as a string segment:

```kotlin
val first: Provider<String> = config.stringAt("items", "0")
```

Sub-trees can be extracted as `YamlMapping` or `YamlSequence`:

```kotlin
val serverBlock: Provider<YamlMapping> = config.mappingAt("server")
val items: Provider<YamlSequence>      = config.sequenceAt("items")
```

### Parse-once, navigate-many

`yamlFile()` parses the file once via a single `ValueSource`. All typed extractions are lazy
`Provider.map()` chains on the result — the file is not re-read for each accessor.

```kotlin
val config = providers.yamlFile(layout.projectDirectory.file("config.yaml"))

tasks.register("printConfig") {
    val host = config.stringAt("server", "host")
    val port = config.intAt("server", "port")
    doLast {
        println("${host.get()}:${port.get()}")
    }
}
```

### Parsing a YAML string

To parse a `Provider<String>` as YAML:

```kotlin
val value: Provider<YamlValue> = someStringProvider.parseYaml()
```

## Installation

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:snakeyaml-extensions")
}
```
