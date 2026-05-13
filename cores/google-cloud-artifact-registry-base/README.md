# Google Cloud Artifact Registry Base

A Kotlin library providing managed Google Cloud Artifact Registry client integration, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:google-cloud-artifact-registry-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `ArtifactRegistryClientBuildService` | `ArtifactRegistryClient` |

```kotlin
val ar = gradle.sharedServices.registerIfAbsent("ar", ArtifactRegistryClientBuildService::class) {
    parameters.credentials.set(FixedCredentialsProvider.create(GoogleCredentials.getApplicationDefault()))
    // credentials is optional; omit to use application default credentials
}
```

Project/location/repository identifiers are not part of the build service — each value source and action
takes its own `projectName`, `location` and `repository` parameters, so a single client can serve calls
against multiple projects and regions.

## Value Source: `GetRepositoryValueSource`

Returns the `Repository` proto for a given repository.

## Value Source: `ListPackagesValueSource`

Returns the fully-qualified resource names of every package in a repository.

## Value Source: `ListVersionsValueSource`

Returns the fully-qualified resource names of every version of a given package.

## Value Source: `ListFilesValueSource`

Returns the fully-qualified resource names of files in a repository. Supports an optional `filter`
expression — see the Artifact Registry `ListFiles` documentation for supported syntax.

## Value Source: `AbstractArtifactValueSource`

Extend this class to read a single Artifact Registry file and transform the response. The `doObtain`
function receives an `InputStream` over the file payload:

```kotlin
abstract class StringArtifactValueSource :
    AbstractArtifactValueSource<String, AbstractArtifactValueSource.Parameters>() {
    override fun doObtain(input: InputStream): String = input.bufferedReader().use { it.readText() }
}
```

Parameters: `service`, `projectName`, `location`, `repository`, `filename`.

## WorkAction: `DownloadFileAction`

Downloads a single Artifact Registry file to a local `RegularFileProperty`:

```kotlin
workerExecutor.noIsolation().submit(DownloadFileAction::class) {
    service.set(ar)
    projectName.set("my-project")
    location.set("us-east1")
    repository.set("my-repo")
    filename.set("artifacts/my-asset.jar")
    outputFile.set(layout.buildDirectory.file("downloads/my-asset.jar"))
}
```

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [Google Cloud Artifact Registry Java Client](https://cloud.google.com/java/docs/reference/google-cloud-artifact-registry/latest/overview)
