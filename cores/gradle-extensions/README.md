# Gradle Extensions

A Kotlin library of extension functions and utility types for Gradle plugin development.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:gradle-extensions")
}
```

## Provider Extensions

### `ProviderFactory` extensions (`ProviderFactoryExtensions`)

| Extension | Description |
|---|---|
| `ofNullable(value)` | Returns a `Provider` with a constant value, or absent if `null` |
| `absent` | Returns an always-absent `Provider` |
| `propertiesFile(file)` | Returns a `Provider<Properties>` backed by a `.properties` file |
| `propertiesFile(Provider<RegularFile>)` | Lazy variant of the above |

### `Provider<T>` caching (`ProviderExtensions`)

`cached(objects)` forces the provider value to be computed and cached in a new backing `Property`. Variants exist for
`Provider<T>`, `Provider<List<T>>`, `Provider<Set<T>>`, and `Provider<Map<K, V>>`.

```kotlin
val cachedValue: Provider<String> = someExpensiveProvider.cached(objects)
```

### `Provider<String>` extensions (`StringProviderExtensions`)

| Extension | Description |
|---|---|
| `filterNotBlank()` | Returns absent if the string is blank |
| `orElseEmpty` | Falls back to `""` if absent |
| `asInt` | Maps to `Int?` via `toIntOrNull()` |
| `asBoolean` | Maps to `Boolean?` via `toBooleanStrictOrNull()` |

### `Provider<FileSystemLocation>` extensions (`FileProviderExtensions`)

| Extension | Description |
|---|---|
| `asAbsolutePath` | Maps to `String` absolute path |
| `asPath` | Maps to `java.nio.file.Path` |
| `Provider<Directory>.dir(path: String)` | Resolves a subdirectory (extends `DirectoryProperty.dir` to generic providers) |
| `Provider<Directory>.dir(path: Provider<String>)` | Lazy variant |
| `Provider<Directory>.file(path: String)` | Resolves a file (extends `DirectoryProperty.file` to generic providers) |
| `Provider<Directory>.file(path: Provider<String>)` | Lazy variant |

### Collection provider extensions (`CollectionProviderExtensions`)

| Extension | Description |
|---|---|
| `Provider<Map<K, V>>.getting(key)` | Returns a `Provider` for a single map value |
| `Provider<Map<K, V>>.getting(key: Provider<K>)` | Lazy key variant |
| `Provider<Iterable<T>>.mapElements(fn)` | Maps collection elements |
| `Provider<Iterable<T>>.mapElementsNotNull(fn)` | Maps elements, dropping nulls |
| `Provider<List<T>>.orElseEmpty` | Falls back to empty list |
| `Provider<Set<T>>.orElseEmpty` | Falls back to empty set |
| `Provider<Map<K, V>>.orElseEmpty` | Falls back to empty map |
| `Provider<Properties>.asMap` | Converts `Properties` to `Map<String, String>` |

## Repository Handler Extensions (`RepositoryHandlerExtensions`)

Convenience methods for common Maven repository configurations:

```kotlin
repositories {
    gitHubPackages("MyRepo", "owner", "repo-name") {
        credentials(PasswordCredentials::class.java)
    }
    gitLabPackages("MyGitLabRepo", "12345678")
    awsCodeArtifact("MyCodeArtifact", "my-domain", "111122223333", "us-east-1", "my-repo")
}
```

## Task Extensions

### `TaskProviderExtensions`

`doLast` and `doFirst` extensions on `TaskProvider<T>` that accept a typed receiver lambda:

```kotlin
tasks.named<MyTask>("myTask").doLast {
    // this: MyTask
    println(myProperty.get())
}
```

### `TaskContainerExtensions`

Typed named lookups for common source-set-derived task names:

```kotlin
tasks.compileJava(sourceSets["main"])          // → TaskProvider<JavaCompile>
tasks.classes(sourceSets["main"])              // → TaskProvider<Task>
tasks.jar(sourceSets["main"])                  // → TaskProvider<Jar>
tasks.sourcesJar(sourceSets["main"])           // → TaskProvider<Jar>
tasks.processResources<Copy>(sourceSets["main"]) // → TaskProvider<Copy>
```

## CI/CD Provider Objects (`ObjectFactoryObjects`)

Three injectable objects expose environment variables for common CI platforms as typed `Provider`s. Obtain them via
`ObjectFactory` extensions:

```kotlin
abstract class MyPlugin @Inject constructor(private val objects: ObjectFactory) : Plugin<Project> {
    override fun apply(project: Project) {
        val gha = objects.githubActions
        val codeBuild = objects.awsCodeBuild
        val combined = objects.githubCodeBuildActions  // GitHub Actions on CodeBuild runners
    }
}
```

### `GitHubActionsProviders`

Provides typed `Provider`s for all
[GitHub Actions default environment variables](https://docs.github.com/en/actions/writing-workflows/choosing-what-your-workflow-does/store-information-in-variables#default-environment-variables),
including `ci`, `sha`, `ref`, `actor`, `repository`, `runId`, and many others. Also provides directory/file `Provider`s
for path-valued variables (e.g. `workspaceDirectory`, `eventFile`, `stepSummaryFile`).

### `AwsCodeBuildProviders`

Provides typed `Provider`s for all
[AWS CodeBuild environment variables](https://docs.aws.amazon.com/codebuild/latest/userguide/build-env-ref-env-vars.html),
including `buildId`, `buildArn`, `resolvedSourceVersion`, `srcDir`, `srcDirs`, and webhook-related variables.

### `GitHubCodeBuildActionsProviders`

Provides typed `Provider`s for the additional environment variables available on
[GitHub Actions runners hosted by AWS CodeBuild](https://docs.aws.amazon.com/codebuild/latest/userguide/build-env-ref-env-vars.html).

## Logging Extensions (`GradleLoggerExtensions`)

Lambda-form logging methods that avoid string construction when the log level is disabled:

```kotlin
logger.debug { "Computed value: $expensive" }
logger.info { "Processing $item" }
logger.warn(exception) { "Failed to process $item" }
logger.lifecycle { "Starting phase" }
```

All methods accept either `message: () -> String` or `(Throwable, () -> String)`. Levels: `debug`, `info`, `lifecycle`,
`quiet`, `warn`, `error`.

## Value Source: `PropertiesFromFileValueSource`

Reads a `.properties` file into a `Provider<Properties>`. Returns absent (rather than throwing) on missing or
malformed files. Use via `ProviderFactory.propertiesFile()`.

## See Also

- [Gradle Providers API](https://docs.gradle.org/current/userguide/lazy_configuration.html)
