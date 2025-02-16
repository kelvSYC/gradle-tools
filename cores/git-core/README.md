# kelvSYC Gradle Tools - Git Core

This plugin contains useful tasks and other tools for working with Git repositories.

## Usage
Some components will require you to apply the Git Core plugin:

```kotlin
plugins {
    id("com.kelvsyc.gradle.git-core")
}
```

Other components will only require you to declare a dependency.

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:git-core")
}
```

## Components
Git Core contains a number of components, some of which require the plugin to be applied.

### `GetGitRemoteArchive` and `GitExport`
The `GetGitRemoteArchive` and `GitExport` task types both use the power of `git archive --remote` to retrieve files from
a specified commit in a remote Git repository. The difference between the two tasks is that `GitExport` extracts the
archive file to the specified destination directory.

Aside from the output, the parameters to both tasks are identical:
* `gitCommand` - The underlying `git` command. By default, the output of `command -v git` will be used.
* `remoteUrl` - The remote repository URL
* `ref` - The remote ref to extract files from
* `paths` - The files and directories within the specified tree to include in the archive. Leave unset to extract all
  files and directories.
* `verbose` - Whether the underlying `git archive --remote` call will have verbose output. Defaults to `false`.

Both tasks are powered by `GitRemoteArchiveAction`, a `WorkAction` implementation that can be used to create other tasks
that extracts files from remote Git repositories. The arguments from both tasks are passed directly to
`GitRemoteArchiveAction`.

### JGit integration
Git Core also contains parts meant to integrate with [JGit](https://github.com/eclipse-jgit/jgit) for users creating
Gradle build logic with the Kotlin DSL.

* `Directory.asRepository` converts a Gradle `Directory` object to a JGit `Repository` object, should the directory
  represent a Git working directory. A similar extension exists for Gradle `Provider`s of `Directory` objects.


