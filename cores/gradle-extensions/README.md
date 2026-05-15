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
| `checksum(file, algorithm)` | Returns a `Provider<String>` with the hex-encoded checksum of a file |
| `checksum(Provider<RegularFile>, algorithm)` | Lazy file variant |
| `checksum(file, Provider<String>)` | Lazy algorithm variant |
| `checksum(Provider<RegularFile>, Provider<String>)` | Fully lazy variant |

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

Injectable objects expose environment variables for common CI platforms as typed `Provider`s. Obtain them via
`ObjectFactory` extensions:

```kotlin
abstract class MyPlugin @Inject constructor(private val objects: ObjectFactory) : Plugin<Project> {
    override fun apply(project: Project) {
        val gha = objects.githubActions
        val codeBuild = objects.awsCodeBuild
        val combined = objects.githubCodeBuildActions  // GitHub Actions on CodeBuild runners
        val azure = objects.azurePipelines
        val circle = objects.circleCI
        val gcb = objects.googleCloudBuild
        val gitlab = objects.gitlabCI
        val gitlabMR = objects.gitlabMergeRequest       // GitLab merge request pipelines
        val tc = objects.teamCity
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

### `AzurePipelinesProviders`

Provides typed `Provider`s for
[Azure Pipelines predefined variables](https://learn.microsoft.com/en-us/azure/devops/pipelines/build/variables),
including agent, build, source control, system, job, stage, pull request, deployment, and pipeline workspace variables.
Sensitive variables (`System.AccessToken`, `System.OidcRequestUri`) are intentionally excluded.

### `CircleCIProviders`

Provides typed `Provider`s for
[CircleCI built-in environment variables](https://circleci.com/docs/variables/#built-in-environment-variables),
including `buildNum`, `sha1`, `branch`, `job`, `pipelineId`, `workflowId`, and parallel run variables. Sensitive
variables (OIDC tokens) are intentionally excluded.

### `GoogleCloudBuildProviders`

Provides typed `Provider`s for
[Google Cloud Build default substitutions](https://cloud.google.com/build/docs/configuring-builds/substitute-variable-values#using_default_substitutions),
including `projectId`, `buildId`, `commitSha`, `branchName`, `triggerName`, and GitHub PR variables. Note that Cloud
Build substitutions must be explicitly passed as environment variables in the build step configuration.

### `GitLabCIProviders`

Provides typed `Provider`s for
[GitLab CI/CD predefined variables](https://docs.gitlab.com/ee/ci/variables/predefined_variables.html), including
server, commit, pipeline, project, job, runner, environment, registry, and user variables. Sensitive variables (tokens,
passwords) are intentionally excluded.

### `GitLabCIMergeRequestProviders`

Provides typed `Provider`s for the
[merge-request-specific predefined variables](https://docs.gitlab.com/ee/ci/variables/predefined_variables.html)
available only in merge request pipelines, including `title`, `sourceBranchName`, `targetBranchName`, `approved`,
`labels`, and diff/branch SHA variables.

### `TeamCityProviders`

Provides typed `Provider`s for
[TeamCity build parameters](https://www.jetbrains.com/help/teamcity/build-script-interaction-with-teamcity.html).
Environment variables (`TEAMCITY_VERSION`, `BUILD_NUMBER`, `BUILD_VCS_NUMBER`, etc.) are read directly. Additional
system properties (`teamcity.build.id`, `teamcity.buildType.id`, `teamcity.build.checkoutDir`, etc.) are read from the
build properties file whose path is given by the `TEAMCITY_BUILD_PROPERTIES_FILE` environment variable, using
`PropertiesFromFileValueSource`.

**Configuration cache and sensitive parameters:** TeamCity writes "Password" type build parameters to the system
properties file and the configuration parameters file in plaintext — the masking in the TeamCity UI and build logs
is display-only. Any `system.*` parameter of "Password" type will appear in the build properties file read by this
class, and the configuration parameters file (path exposed via `configurationPropertiesFilePath`) may also contain
password-type parameters.

The properties exposed by `TeamCityProviders` are limited to well-known, non-sensitive system properties. However,
if you access additional properties from the same files — for example to read a custom `system.myPassword` parameter
— those values will be serialized to the Gradle configuration cache in plaintext. Read sensitive TeamCity
parameters inside a `WorkAction` at task execution time instead.

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

## Value Sources

**Configuration cache behaviour:** Gradle serializes the result of every `ValueSource.obtain()` call to the
configuration cache when the cache is written, and reuses that result on subsequent builds without re-running
`obtain()`. Two implications follow:

- **Staleness:** The cached result reflects the state at the time the cache was written. External state — file
  contents, API responses, environment variables — is not re-read on cache hits. For file-backed sources, Gradle
  uses the input file as a cache key and invalidates the cache when the file changes. For sources that call
  external services or read values that change independently of the inputs declared in `Parameters`, the cached
  result may be stale until the cache is explicitly invalidated (e.g. `--rerun-tasks` or deleting
  `.gradle/configuration-cache/`).

- **Plaintext storage:** The serialized result is stored in plaintext in `.gradle/configuration-cache/`. If
  `obtain()` returns sensitive values (passwords, tokens, secrets), those values are readable by any process with
  access to the build directory. Retrieve sensitive values at task execution time instead — either inside a
  `@TaskAction` body or inside a `WorkAction.execute()` body — by calling `ProviderFactory` (or our extensions)
  directly there. Values obtained this way are resolved after the cache has been read and are never written to it.
  The unsafe pattern is wiring a `Provider` into a task `@Input` property, which forces resolution at cache-write
  time; calling `providers.of(...)` or `providers.environmentVariable(...)` entirely within task or work action
  code is safe.

### `AbstractResourceValueSource`

Abstract base class for `ValueSource` implementations that read a resource bundled in the plugin JAR. Subclasses
implement `doObtain(input: InputStream): T?` to transform the resource stream into the desired type.

```kotlin
abstract class MyConfigValueSource :
    AbstractResourceValueSource<Properties, AbstractResourceValueSource.Parameters>() {
    override fun doObtain(input: InputStream): Properties =
        Properties().apply { load(input) }
}

// Usage:
val config: Provider<Properties> = providers.of(MyConfigValueSource::class.java) {
    it.parameters.resourcePath.set("defaults.properties")
}
```

Extend the `Parameters` interface if your subclass needs additional configuration beyond the resource path.

### `StringResourceValueSource`

Reads a classpath resource as a `String`.

```kotlin
val version: Provider<String> = providers.of(StringResourceValueSource::class.java) {
    it.parameters.resourcePath.set("version.txt")
}
```

### `PropertiesResourceValueSource`

Reads a classpath resource as a `Properties` object.

```kotlin
val defaults: Provider<Properties> = providers.of(PropertiesResourceValueSource::class.java) {
    it.parameters.resourcePath.set("defaults.properties")
}
```

### `PropertiesFromFileValueSource`

Reads a `.properties` file into a `Provider<Properties>`. Returns absent (rather than throwing) on missing or
malformed files. Use via `ProviderFactory.propertiesFile()`.

**Configuration cache:** The entire `Properties` result is serialized to the Gradle configuration cache in
plaintext when the cache is written. If the file contains sensitive values (passwords, tokens, API keys), those
values will be stored in `.gradle/configuration-cache/`. Only use this source with files whose complete contents
can be safely cached. When sensitive values are required at task execution time, read the file inside a
`WorkAction` instead.

### `ChecksumValueSource`

Computes the checksum of a file using a specified digest algorithm (e.g. `SHA-256`, `SHA-512`, `MD5`) and returns
the result as a lowercase hex-encoded `Provider<String>`. Returns absent on missing files or unsupported algorithms.
Use via `ProviderFactory.checksum()`.

```kotlin
val sha256: Provider<String> = providers.checksum(layout.projectDirectory.file("artifact.jar"), "SHA-256")
```

## Work Actions

### `ChecksumWorkAction`

A `WorkAction` that computes the checksum of an input file and writes the hex-encoded result to a specified output file.

```kotlin
workerExecutor.noIsolation().submit(ChecksumWorkAction::class) {
    inputFile.set(layout.buildDirectory.file("libs/my-lib.jar"))
    algorithm.set("SHA-256")
    outputFile.set(layout.buildDirectory.file("libs/my-lib.jar.sha256"))
}
```

### `ZipAction`

A `WorkAction` that creates a ZIP archive from a set of input files. This is **not** a replacement for the built-in
[`Zip`](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.bundling.Zip.html) task type — new tasks that solely
produce an archive should continue to use `Zip`. Instead, `ZipAction` is a migration aid for legacy tasks that create
archives as one step among many (e.g. inline `ant.zip` calls), allowing that step to participate in the Worker API
without requiring decomposition into a separate task.

```kotlin
workerExecutor.noIsolation().submit(ZipAction::class.java) {
    baseDirectory.set(layout.buildDirectory.dir("staging"))
    sourceFiles.from(fileTree(layout.buildDirectory.dir("staging")))
    outputFile.set(layout.buildDirectory.file("output/archive.zip"))
    compressionLevel.set(6) // optional; defaults to JDK default
}
```

| Parameter | Type | Description |
|---|---|---|
| `baseDirectory` | `DirectoryProperty` | Base directory for computing relative ZIP entry paths |
| `sourceFiles` | `ConfigurableFileCollection` | Files to include in the archive |
| `outputFile` | `RegularFileProperty` | The output ZIP file |
| `compressionLevel` | `Property<Int>` | Compression level (0–9); optional |

## See Also

- [Gradle Providers API](https://docs.gradle.org/current/userguide/lazy_configuration.html)
