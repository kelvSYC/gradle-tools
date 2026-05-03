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

## GitHub Support

Requires the [GitHub CLI (`gh`)](https://cli.github.com/) to be installed. The plugin sets the `ghCommand` convention
on all `GetGitHubRepoArchive` tasks via `command -v gh`.

### `GetGitHubRepoArchive`

Downloads a GitHub repository tree at a specific ref as an archive, using `gh api`. Supported formats are `.tar.gz`
(and `.tgz`) and `.zip`, which map to the GitHub `tarball` and `zipball` API endpoints respectively.

```kotlin
tasks.register<GetGitHubRepoArchive>("fetchSource") {
    owner.set("example")
    repo.set("my-repo")
    ref.set("v2.0.0")
    token.set(providers.environmentVariable("GITHUB_TOKEN"))
    outputFile.set(layout.buildDirectory.file("source.tar.gz"))
}
```

| Property | Type | Description |
|---|---|---|
| `ghCommand` | `Property<String>` | Path to the `gh` binary. Set via convention when plugin is applied. |
| `hostname` | `Property<String>` | GitHub Enterprise hostname. Leave unset for GitHub.com. |
| `token` | `Property<String>` | Personal access token (`GH_TOKEN` or `GH_ENTERPRISE_TOKEN`). |
| `owner` | `Property<String>` | Repository owner |
| `repo` | `Property<String>` | Repository name |
| `ref` | `Property<String>` | Commit ref to archive |
| `outputFile` | `RegularFileProperty` | Output archive file (`.tar.gz`, `.tgz`, or `.zip`) |

### `WorkAction`: `GitHubRepoArchiveAction`

The task delegates to `GitHubRepoArchiveAction`. Use it directly for custom tasks.

### `WorkAction`: `DownloadGitHubReleaseArtifactAction`

Downloads assets from a GitHub release using `gh release download`.

```kotlin
abstract class MyTask @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {
    @TaskAction
    fun run() {
        workers.noIsolation().submit(DownloadGitHubReleaseArtifactAction::class) {
            owner.set("example")
            repo.set("my-repo")
            tag.set("v2.0.0")
            patternGlobs.add("*.jar")
            outputDirectory.set(layout.buildDirectory.dir("libs"))
        }
    }
}
```

| Parameter | Type | Description |
|---|---|---|
| `ghCommand` | `Property<String>` | Path to the `gh` binary |
| `hostname` | `Property<String>` | GitHub Enterprise hostname. Leave unset for GitHub.com. |
| `token` | `Property<String>` | Personal access token |
| `owner` | `Property<String>` | Repository owner |
| `repo` | `Property<String>` | Repository name |
| `tag` | `Property<String>` | Release tag |
| `patternGlobs` | `ListProperty<String>` | Glob patterns to filter assets. Leave empty for all assets. |
| `outputDirectory` | `DirectoryProperty` | Directory to download assets into |

## GitLab Support

Requires the [GitLab CLI (`glab`)](https://gitlab.com/gitlab-org/cli) to be installed. The plugin sets the
`glabCommand` convention on all `GetGitLabRepoArchive` tasks via `command -v glab`.

For self-hosted GitLab instances, set `hostname` to your instance's hostname. The hostname is embedded in the
repository path passed to `glab` as `hostname/owner/repo`. The token is always supplied as `GITLAB_TOKEN`.

### `GetGitLabRepoArchive`

Downloads a GitLab repository tree at a specific ref as an archive, using `glab repo archive`. Supported formats are
`.tar.gz`/`.tgz`, `.tar.bz2`/`.tbz2`, `.tar`, and `.zip`.

```kotlin
tasks.register<GetGitLabRepoArchive>("fetchSource") {
    owner.set("example-group")
    repo.set("my-project")
    ref.set("v2.0.0")
    token.set(providers.environmentVariable("GITLAB_TOKEN"))
    outputFile.set(layout.buildDirectory.file("source.tar.gz"))
}
```

| Property | Type | Description |
|---|---|---|
| `glabCommand` | `Property<String>` | Path to the `glab` binary. Set via convention when plugin is applied. |
| `hostname` | `Property<String>` | Self-hosted GitLab hostname. Leave unset for GitLab.com. |
| `token` | `Property<String>` | Personal access token (`GITLAB_TOKEN`). |
| `owner` | `Property<String>` | Namespace or group owning the repository |
| `repo` | `Property<String>` | Repository name |
| `ref` | `Property<String>` | Commit ref to archive |
| `outputFile` | `RegularFileProperty` | Output archive file (`.tar.gz`, `.tgz`, `.tar.bz2`, `.tbz2`, `.tar`, or `.zip`) |

### `WorkAction`: `GitLabRepoArchiveAction`

The task delegates to `GitLabRepoArchiveAction`. Use it directly for custom tasks.

### `WorkAction`: `DownloadGitLabReleaseArtifactAction`

Downloads assets from a GitLab release using `glab release download`.

```kotlin
abstract class MyTask @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {
    @TaskAction
    fun run() {
        workers.noIsolation().submit(DownloadGitLabReleaseArtifactAction::class) {
            owner.set("example-group")
            repo.set("my-project")
            tag.set("v2.0.0")
            assetNames.add("*.jar")
            outputDirectory.set(layout.buildDirectory.dir("libs"))
        }
    }
}
```

| Parameter | Type | Description |
|---|---|---|
| `glabCommand` | `Property<String>` | Path to the `glab` binary |
| `hostname` | `Property<String>` | Self-hosted GitLab hostname. Leave unset for GitLab.com. |
| `token` | `Property<String>` | Personal access token |
| `owner` | `Property<String>` | Namespace or group owning the repository |
| `repo` | `Property<String>` | Repository name |
| `tag` | `Property<String>` | Release tag |
| `assetNames` | `ListProperty<String>` | Asset name filters (supports globs). Leave empty for all assets. |
| `outputDirectory` | `DirectoryProperty` | Directory to download assets into |

## See Also

- [JGit](https://github.com/eclipse-jgit/jgit) — The Java Git library used for repository introspection
