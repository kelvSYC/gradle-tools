# Google Cloud Functions Base

A Kotlin library providing managed Cloud Functions Gen 2 client integration using the Google Cloud
Java SDK, built on `clients-base` and `google-cloud-extensions`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:google-cloud-functions-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `FunctionServiceClientBuildService` | `FunctionServiceClient` (Cloud Functions v2 admin API) |

```kotlin
val functions = gradle.sharedServices.registerIfAbsent(
    "functions",
    FunctionServiceClientBuildService::class,
) {
    parameters {
        projectId.set("my-project")
        applicationDefault()
    }
}
```

Both `projectId` and the credentials call are optional. See
[google-cloud-extensions](../google-cloud-extensions) for the full set of credential configuration
functions.

## Value Sources

### `GetFunctionValueSource`

Retrieves the HTTPS trigger URI of a Gen 2 function. Returns `null` if the function has no URI
(e.g. still provisioning):

```kotlin
val functionUri: Provider<String> = providers.of(GetFunctionValueSource::class) {
    parameters {
        service.set(functions)
        functionName.set("projects/my-project/locations/us-central1/functions/my-function")
    }
}
```

### `ListFunctionsValueSource`

Lists all Gen 2 functions in a project and location, returning a `Map<String, String>` keyed by
short function name with the HTTPS trigger URI as the value. Pagination is handled internally:

```kotlin
val allFunctions: Provider<Map<String, String>> = providers.of(ListFunctionsValueSource::class) {
    parameters {
        service.set(functions)
        projectId.set("my-project")
        location.set("us-central1")
    }
}
```

## WorkActions

### `CallFunctionAction`

Invokes a Gen 2 function via HTTP POST. The response body is discarded. Provide an OIDC identity
token via `identityToken` for authenticated functions:

```kotlin
workerExecutor.noIsolation().submit(CallFunctionAction::class) {
    uri.set("https://us-central1-my-project.cloudfunctions.net/my-function")
    identityToken.set(CredentialReference.EnvironmentVariable("FUNCTION_IDENTITY_TOKEN"))
    payload.set("""{"event":"deploy"}""")  // optional
}
```

| Parameter | Type | Description |
|---|---|---|
| `uri` | `Property<String>` | Full HTTPS trigger URL |
| `identityToken` | `Property<CredentialReference>` | Optional OIDC token reference for `Authorization: Bearer` |
| `payload` | `Property<String>` | Optional request body |

### `UpdateFunctionAction`

Updates a function's source to a zip already staged in Google Cloud Storage:

```kotlin
workerExecutor.noIsolation().submit(UpdateFunctionAction::class) {
    service.set(functions)
    functionName.set("projects/my-project/locations/us-central1/functions/my-function")
    bucket.set("my-deploy-bucket")
    storageObject.set("releases/v1.2.3/function.zip")
    storageGeneration.set(1234567890L)  // optional
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<FunctionServiceClientBuildService>` | Build service supplying the admin client |
| `functionName` | `Property<String>` | Full resource name of the function |
| `bucket` | `Property<String>` | GCS bucket containing the deployment zip |
| `storageObject` | `Property<String>` | GCS object name of the deployment zip |
| `storageGeneration` | `Property<Long>` | Optional object generation to pin |

### `UploadAndUpdateFunctionAction`

Uploads a local zip via a signed URL and updates the function in one step. Equivalent to calling
`generateUploadUrl`, HTTP PUT the zip, then `updateFunction`:

```kotlin
workerExecutor.noIsolation().submit(UploadAndUpdateFunctionAction::class) {
    service.set(functions)
    functionName.set("projects/my-project/locations/us-central1/functions/my-function")
    zipFile.set(layout.buildDirectory.file("dist/function.zip"))
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<FunctionServiceClientBuildService>` | Build service supplying the admin client |
| `functionName` | `Property<String>` | Full resource name of the function |
| `zipFile` | `RegularFileProperty` | Local zip to upload |

## Tasks

### `DeployFunctionFromZip` / `AbstractDeployFunctionFromZip`

A `DefaultTask` wrapper around `UploadAndUpdateFunctionAction` that declares `zipFile` as an
`@InputFile`, enabling Gradle up-to-date checking and wiring to an upstream task that produces
the zip:

```kotlin
tasks.register<DeployFunctionFromZip>("deploy") {
    service.set(functions)
    functionName.set("projects/my-project/locations/us-central1/functions/my-function")
    zipFile.set(tasks.named<Zip>("packageFunction").flatMap { it.archiveFile })
}
```

Use `AbstractDeployFunctionFromZip` only when you need to supply a custom `service` binding
without `@ServiceReference` tracking.

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [google-cloud-extensions](../google-cloud-extensions) — `GcpBuildServiceParams` and credential extensions
- [google-cloud-storage-base](../google-cloud-storage-base) — Upload zips to GCS before using `UpdateFunctionAction`
