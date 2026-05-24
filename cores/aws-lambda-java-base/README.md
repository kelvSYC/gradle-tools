# AWS Lambda Java Base

A Kotlin library providing managed AWS Lambda client integration using the AWS SDK for Java, built on
`clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-lambda-java-base")
}
```

## Build Services

| Class | Client type | Use case |
|---|---|---|
| `LambdaClientBuildService` | `LambdaClient` | Synchronous Lambda operations |
| `LambdaAsyncClientBuildService` | `LambdaAsyncClient` | Asynchronous Lambda operations |

Register a build service from a plugin or `build.gradle.kts`:

```kotlin
val lambda = gradle.sharedServices.registerIfAbsent("lambda", LambdaClientBuildService::class) {
    parameters {
        regionId.set("us-east-1")
        defaultCredentials()
    }
}
val lambdaAsync = gradle.sharedServices.registerIfAbsent("lambdaAsync", LambdaAsyncClientBuildService::class) {
    parameters {
        regionId.set("us-east-1")
        defaultCredentials()
    }
}
```

Both parameters are optional. Leave `region` unset to fall back to the SDK's `DefaultAwsRegionProviderChain`,
and leave `credentials` unset to fall back to anonymous credentials.

## Value Sources

### `ListFunctionsValueSource`

Lists all Lambda functions visible to the configured client, returned as a `Map<String, String>` keyed by
function name with the function ARN as the value. Pagination is handled internally:

```kotlin
val functions: Provider<Map<String, String>> = providers.of(ListFunctionsValueSource::class) {
    parameters {
        service.set(lambda)
    }
}
```

### `GetFunctionConfigurationValueSource`

Retrieves the ARN of a Lambda function's published configuration (qualified by version or alias when set):

```kotlin
val arn: Provider<String> = providers.of(GetFunctionConfigurationValueSource::class) {
    parameters {
        service.set(lambda)
        functionName.set("my-fn")
        qualifier.set("prod")   // optional version or alias
    }
}
```

Returns `null` and logs a warning if the call throws `LambdaException` (e.g. function not found).

## WorkActions

### `InvokeFunctionAction`

Invokes a Lambda function. Fire-and-forget — the response payload is discarded:

```kotlin
workerExecutor.noIsolation().submit(InvokeFunctionAction::class) {
    service.set(lambda)
    functionName.set("my-fn")
    qualifier.set("prod")                       // optional
    payload.set("{\"hello\":\"world\"}")        // optional UTF-8 input
    invocationType.set("Event")                 // optional: RequestResponse | Event | DryRun
}
```

### `UpdateFunctionCodeAction`

Updates the deployment package (zip file) for a Lambda function:

```kotlin
workerExecutor.noIsolation().submit(UpdateFunctionCodeAction::class) {
    service.set(lambda)
    functionName.set("my-fn")
    zipFile.set(layout.buildDirectory.file("dist/my-fn.zip"))
    publish.set(true)   // optional; defaults to false
}
```

## Tasks: Batch Code Update

Updates the deployment package for multiple Lambda functions. Coordinates and content are specified
per-artifact. All artifacts must be registered via `registerArtifact`.

Two concurrency models are available — choose based on which client you use:

| Class | Client type | Concurrency |
|---|---|---|
| `BatchUpdateFunctionCode` | `LambdaClient` (sync) | Sequential |
| `AsyncBatchUpdateFunctionCode` | `LambdaAsyncClient` (async) | `CompletableFuture.allOf` |

For BYO-client usage (without auto-registered build services), extend `AbstractSyncBatchUpdateFunctionCode`
(sync) or `AbstractAsyncBatchUpdateFunctionCode` (async) and set the `service` or `client` property directly.

```kotlin
val updateAll = tasks.register<BatchUpdateFunctionCode>("updateAll") {
    service.set(lambda)
    registerArtifact("api") {
        it.functionName.set("my-api-fn")
        it.zipFile.set(layout.buildDirectory.file("dist/api.zip"))
        it.publish.set(true)
    }
    registerArtifact("worker") {
        it.functionName.set("my-worker-fn")
        it.zipFile.set(layout.buildDirectory.file("dist/worker.zip"))
        it.publish.set(true)
    }
    // waitForActive.set(false)  // opt out of post-update Active state wait
    versionArnsFile.set(layout.buildDirectory.file("lambda/version-arns.json"))
}

// Wire the version ARNs file to a downstream task without forcing evaluation:
val arnsFile: Provider<RegularFile> = updateAll.flatMap { it.versionArnsFile }
```

The `versionArnsFile` JSON format is `{"functionName": "arn:aws:lambda:…:function:my-fn:42", …}`.

| Per-artifact property | Type | Description |
|---|---|---|
| `functionName` | `Property<String>` | Function name, ARN, or partial ARN |
| `zipFile` | `RegularFileProperty` | Path to the zip file to upload |
| `publish` | `Property<Boolean>` | Whether to publish a new version (optional) |

| Task property | Type | Description |
|---|---|---|
| `service` / `client` | `Property<…BuildService>` | Build service (or raw async client) |
| `waitForActive` | `Property<Boolean>` | Wait for `LastUpdateStatus = Successful` after each upload (default `true`) |
| `versionArnsFile` | `RegularFileProperty` | Optional file to write published version ARN JSON into |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-lambda-kotlin-base](../aws-lambda-kotlin-base) — Kotlin SDK variant
