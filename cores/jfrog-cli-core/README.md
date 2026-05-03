# JFrog CLI Core

A Gradle plugin providing tasks and utilities for working with JFrog Artifactory via the
[JFrog CLI (`jf`)](https://docs.jfrog-applications.jfrog.io/jfrog-applications/jfrog-cli).

Requires the JFrog CLI to be installed and available on `PATH`. The plugin sets the `jfCommand` convention on all
tasks via `which jf`.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.jfrog-cli-core")
}
```

## Build Info

The build info tasks wrap the `jf rt build-*` family of commands. Build info accumulates locally (in
`~/.jfrog/projects/`) across multiple tasks before being published to Artifactory in a single
`PublishBuildInfo` step. The typical lifecycle is:

1. `AddGitInfoToBuild` — capture VCS metadata
2. `CollectBuildEnvironment` — snapshot environment variables
3. `PublishBuildInfo` — push accumulated info to Artifactory
4. `ScanBuild` — trigger an Xray security scan (optional, requires Xray)

`CleanBuildInfo` can be used at the start of a pipeline to discard any leftover state from a previous run.

### `AddGitInfoToBuild`

Captures Git VCS metadata (commit, branch, remote URL) into the local build info using `jf rt build-add-git`.
Does not require a server connection.

```kotlin
tasks.register<AddGitInfoToBuild>("addGitInfo") {
    buildName.set("my-app")
    buildNumber.set(providers.environmentVariable("BUILD_NUMBER"))
}
```

| Property | Type | Description |
|---|---|---|
| `jfCommand` | `Property<String>` | Path to the `jf` binary. Set via convention when plugin is applied. |
| `buildName` | `Property<String>` | Build name as it will appear in Artifactory |
| `buildNumber` | `Property<String>` | Build number |

### `CollectBuildEnvironment`

Snapshots environment variables into the local build info using `jf rt build-collect-env`.
Does not require a server connection.

```kotlin
tasks.register<CollectBuildEnvironment>("collectEnv") {
    buildName.set("my-app")
    buildNumber.set(providers.environmentVariable("BUILD_NUMBER"))
    includePatterns.add("CI_*")            // optional: include only matching vars
    excludePatterns.add("*SECRET*")        // optional: exclude matching vars
}
```

Patterns are glob expressions joined with `;` and passed to `--include-vars` / `--exclude-vars`.

| Property | Type | Description |
|---|---|---|
| `jfCommand` | `Property<String>` | Path to the `jf` binary. Set via convention when plugin is applied. |
| `buildName` | `Property<String>` | Build name |
| `buildNumber` | `Property<String>` | Build number |
| `includePatterns` | `ListProperty<String>` | Glob patterns for variables to include. Leave empty to include all. |
| `excludePatterns` | `ListProperty<String>` | Glob patterns for variables to exclude. Leave empty to exclude none. |

### `PublishBuildInfo`

Publishes the accumulated local build info to Artifactory using `jf rt build-publish`.
Requires a server connection.

```kotlin
tasks.register<PublishBuildInfo>("publishBuildInfo") {
    serverUrl.set("https://artifactory.example.com")
    accessToken.set(providers.environmentVariable("JFROG_ACCESS_TOKEN"))
    buildName.set("my-app")
    buildNumber.set(providers.environmentVariable("BUILD_NUMBER"))
    envExcludePatterns.add("*PASSWORD*")   // optional: strip sensitive vars before publishing
}
```

| Property | Type | Description |
|---|---|---|
| `jfCommand` | `Property<String>` | Path to the `jf` binary. Set via convention when plugin is applied. |
| `serverUrl` | `Property<String>` | Artifactory server URL |
| `accessToken` | `Property<String>` | JFrog access token for authentication |
| `buildName` | `Property<String>` | Build name |
| `buildNumber` | `Property<String>` | Build number |
| `envExcludePatterns` | `ListProperty<String>` | Glob patterns for env var names to strip before publishing. |

### `CleanBuildInfo`

Deletes locally accumulated build info using `jf rt build-clean`. Does not affect any already-published
build info on the Artifactory server. Does not require a server connection.

```kotlin
tasks.register<CleanBuildInfo>("cleanBuildInfo") {
    buildName.set("my-app")
    buildNumber.set(providers.environmentVariable("BUILD_NUMBER"))
}
```

| Property | Type | Description |
|---|---|---|
| `jfCommand` | `Property<String>` | Path to the `jf` binary. Set via convention when plugin is applied. |
| `buildName` | `Property<String>` | Build name |
| `buildNumber` | `Property<String>` | Build number |

### `ScanBuild`

Triggers an Xray security scan on a published build using `jf rt build-scan`. Requires a server connection
with Xray enabled. The build must already be published before scanning — use `PublishBuildInfo` first.

```kotlin
tasks.register<ScanBuild>("scanBuild") {
    serverUrl.set("https://artifactory.example.com")
    accessToken.set(providers.environmentVariable("JFROG_ACCESS_TOKEN"))
    buildName.set("my-app")
    buildNumber.set(providers.environmentVariable("BUILD_NUMBER"))
    failBuild.set(true)    // optional: fail the build on policy violations
}
```

| Property | Type | Description |
|---|---|---|
| `jfCommand` | `Property<String>` | Path to the `jf` binary. Set via convention when plugin is applied. |
| `serverUrl` | `Property<String>` | Artifactory server URL |
| `accessToken` | `Property<String>` | JFrog access token for authentication |
| `buildName` | `Property<String>` | Build name |
| `buildNumber` | `Property<String>` | Build number |
| `failBuild` | `Property<Boolean>` | Fail the build if Xray policy violations are found. Defaults to `false`. |

## WorkActions

Each task delegates to a corresponding `WorkAction`. Use them directly to build custom tasks:

```kotlin
abstract class MyTask @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {
    @TaskAction
    fun run() {
        workers.noIsolation().submit(PublishBuildInfoAction::class) {
            serverUrl.set("https://artifactory.example.com")
            accessToken.set("...")
            buildName.set("my-app")
            buildNumber.set("42")
        }
    }
}
```

| WorkAction | Wraps |
|---|---|
| `AddGitInfoToBuildAction` | `jf rt build-add-git` |
| `CollectBuildEnvironmentAction` | `jf rt build-collect-env` |
| `PublishBuildInfoAction` | `jf rt build-publish` |
| `CleanBuildInfoAction` | `jf rt build-clean` |
| `ScanBuildAction` | `jf rt build-scan` |

## Artifact Search Value Source

`AbstractArtifactorySearchValueSource` runs `jf rt search` at configuration time and makes the JSON
output available to the build graph as a typed `Provider`. Use it to query artifact metadata before tasks
run — for example, to check whether a version already exists or to find the latest build artifact.

Extend it by implementing `buildSearchArgs()` (which CLI arguments identify the search) and
`doObtain(output: String)` (which parses the JSON into your desired type):

```kotlin
abstract class LatestVersionValueSource
@Inject constructor(execOperations: ExecOperations) :
    AbstractArtifactorySearchValueSource<String, LatestVersionValueSource.Parameters>(execOperations) {

    interface Parameters : AbstractArtifactorySearchValueSource.Parameters {
        val repoKey: Property<String>
        val artifactName: Property<String>
    }

    override fun buildSearchArgs() =
        listOf("${parameters.repoKey.get()}/${parameters.artifactName.get()}-*.jar")

    override fun doObtain(output: String): String? =
        // Parse the JSON array and extract the latest version from artifact paths
        output.lines()
            .filter { it.contains("\"path\"") }
            .lastOrNull()
            ?.substringAfter("\"path\" : \"")
            ?.substringBefore("\"")
}
```

Use it via `providers.of(...)`:

```kotlin
val latestVersion: Provider<String> = providers.of(LatestVersionValueSource::class) {
    parameters {
        jfCommand.set(providers.which("jf"))
        serverUrl.set("https://artifactory.example.com")
        accessToken.set(providers.environmentVariable("JFROG_ACCESS_TOKEN"))
        repoKey.set("libs-release-local")
        artifactName.set("my-app")
    }
}
```

Base parameters declared in `AbstractArtifactorySearchValueSource.Parameters`:

| Parameter | Type | Description |
|---|---|---|
| `jfCommand` | `Property<String>` | Path to the `jf` binary |
| `serverUrl` | `Property<String>` | Artifactory server URL. Leave unset to use the CLI's configured default. |
| `accessToken` | `Property<String>` | JFrog access token. Leave unset to use the CLI's configured credentials. |

## See Also

- [JFrog CLI documentation](https://docs.jfrog-applications.jfrog.io/jfrog-applications/jfrog-cli) — Full reference for all `jf` commands
- [JFrog Build Info](https://www.jfrog.com/confluence/display/JFROG/Build+Integration) — Artifactory build integration concepts
- [Xray Scanning](https://www.jfrog.com/confluence/display/JFROG/Xray+Scanning) — Security scanning with JFrog Xray
