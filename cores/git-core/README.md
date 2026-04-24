# Git Core

A Gradle plugin and library providing tasks and utilities for working with remote Git repositories and JGit.

## Applying the Plugin

The plugin is required for the `GetGitRemoteArchive` and `GitExport` tasks; it sets the `gitCommand` convention via
`command -v git`. If you only need the JGit extensions, declare a dependency without applying the plugin:

```kotlin
// Plugin (required for GetGitRemoteArchive / GitExport tasks)
plugins {
    id("com.kelvsyc.gradle.git-core")
}

// Library only (for JGit extensions)
dependencies {
    implementation("com.kelvsyc.gradle:git-core")
}
```

## Tasks

### `GetGitRemoteArchive`

Downloads a remote Git repository tree at a specific ref into an archive file, using `git archive --remote`. Supported
archive formats are those recognized by `git archive --list` (typically `tar`, `zip`, `tar.gz`/`tgz`).

```kotlin
tasks.register<GetGitRemoteArchive>("fetchConfig") {
    remoteUrl.set("git@github.com:example/repo.git")
    ref.set("main")
    paths.add("config/")           // optional: restrict to specific paths
    outputFile.set(layout.buildDirectory.file("config.tar.gz"))
}
```

The `gitCommand` property defaults to the result of `command -v git` when the plugin is applied.

| Property | Type | Description |
|---|---|---|
| `gitCommand` | `Property<String>` | Path to the `git` binary. Set via convention when plugin is applied. |
| `remoteUrl` | `Property<String>` | Remote repository URL |
| `ref` | `Property<String>` | Commit ref to archive |
| `paths` | `ListProperty<String>` | Paths within the tree to include. Leave empty for all. |
| `outputFile` | `RegularFileProperty` | Output archive file |
| `verbose` | `Property<Boolean>` | Enable `--verbose` output. Defaults to `false`. |

### `GitExport`

Identical to `GetGitRemoteArchive` but extracts the archive into a directory instead of writing an archive file. The
archive format used internally is always `zip`.

```kotlin
tasks.register<GitExport>("exportConfig") {
    remoteUrl.set("git@github.com:example/repo.git")
    ref.set("v1.2.3")
    paths.add("config/")
    outputDirectory.set(layout.buildDirectory.dir("config"))
}
```

| Property | Type | Description |
|---|---|---|
| `gitCommand` | `Property<String>` | Path to the `git` binary. Set via convention when plugin is applied. |
| `remoteUrl` | `Property<String>` | Remote repository URL |
| `ref` | `Property<String>` | Commit ref to archive |
| `paths` | `ListProperty<String>` | Paths within the tree to include. Leave empty for all. |
| `outputDirectory` | `DirectoryProperty` | Output directory |
| `verbose` | `Property<Boolean>` | Enable `--verbose` output. Defaults to `false`. |

## `WorkAction`: `GitRemoteArchiveAction`

Both tasks delegate to `GitRemoteArchiveAction`. Use it directly to build custom tasks that download remote archives:

```kotlin
abstract class MyTask @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {
    @TaskAction
    fun run() {
        workers.noIsolation().submit(GitRemoteArchiveAction::class) {
            remoteUrl.set("git@github.com:example/repo.git")
            ref.set("main")
            outputFile.set(layout.buildDirectory.file("archive.zip"))
        }
    }
}
```

## JGit Extensions

`GitRepositoryExtensions` provides extensions to convert Gradle `Directory` objects to JGit `Repository` objects:

```kotlin
// Open the project root as a JGit Repository
val repo: Repository? = layout.projectDirectory.asRepository

// Or from a Provider<Directory>
val repoProvider: Provider<Repository?> = layout.projectDirectory.let {
    providers.provider { it }
}.asRepository
```

`asRepository` returns `null` (or an absent `Provider`) if the directory is not a Git working tree.

## Shell Utility

`ProviderFactory.which(command)` uses `command -v` to locate an executable on `PATH`:

```kotlin
val gitPath: Provider<String> = providers.which("git")
```

## See Also

- [JGit](https://github.com/eclipse-jgit/jgit) — The Java Git library used for repository introspection
