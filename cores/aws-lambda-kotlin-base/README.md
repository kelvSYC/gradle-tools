# AWS Lambda Kotlin Base

A Kotlin library providing managed AWS Lambda client integration using the AWS SDK for Kotlin, built on
`clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-lambda-kotlin-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `LambdaClientBuildService` | `LambdaClient` (AWS SDK for Kotlin) |

```kotlin
val lambda = gradle.sharedServices.registerIfAbsent("lambda", LambdaClientBuildService::class) {
    parameters {
        region.set("us-east-1")
        from(providers.credentials(AwsCredentials::class.java, "lambda"))
    }
}
```

Both `region` and the credentials extension call are optional. Leave `region` unset to use the AWS SDK for Kotlin
default region provider chain. Omit the credentials call to skip the `credentialsProvider` assignment, in which
case the SDK applies its own default behavior. See [aws-kotlin-extensions](../aws-kotlin-extensions) for the full
set of credential configuration functions.

## Value Sources

### `GetFunctionConfigurationValueSource`

Retrieves a Lambda function's ARN (qualified by version or alias when set):

```kotlin
val functionArn: Provider<String> = providers.of(GetFunctionConfigurationValueSource::class) {
    parameters {
        service.set(lambda)
        functionName.set("my-fn")
        qualifier.set("prod") // optional
    }
}
```

Returns `null` and logs a warning if the call throws `LambdaException`.

### `ListFunctionsValueSource`

Lists all Lambda functions visible to the configured client, returning a `Map<String, String>` keyed by function
name with the function ARN as the value. Pagination is handled internally:

```kotlin
val functions: Provider<Map<String, String>> = providers.of(ListFunctionsValueSource::class) {
    parameters {
        service.set(lambda)
    }
}
```

## Tasks

### `InvokeFunction`

Invokes a Lambda function (fire-and-forget — the response payload is discarded):

```kotlin
tasks.register<InvokeFunction>("invokeLambda") {
    service.set(lambda)
    functionName.set("my-fn")
    qualifier.set("prod")          // optional
    payload.set("{\"hello\":\"world\"}") // optional UTF-8 payload
    invocationType.set("Event")     // optional: RequestResponse, Event, DryRun
}
```

| Property | Type | Description |
|---|---|---|
| `service` | `Property<LambdaClientBuildService>` | Build service supplying the Lambda client |
| `functionName` | `Property<String>` | Function name, ARN, or partial ARN |
| `qualifier` | `Property<String>` | Optional version or alias |
| `payload` | `Property<String>` | Optional UTF-8 payload |
| `invocationType` | `Property<String>` | One of `RequestResponse`, `Event`, `DryRun` |

### `UpdateFunctionCode`

Uploads a new deployment package zip to an existing Lambda function:

```kotlin
tasks.register<UpdateFunctionCode>("updateLambdaCode") {
    service.set(lambda)
    functionName.set("my-fn")
    zipFile.set(layout.buildDirectory.file("dist/my-fn.zip"))
    publish.set(true)
}
```

| Property | Type | Description |
|---|---|---|
| `service` | `Property<LambdaClientBuildService>` | Build service supplying the Lambda client |
| `functionName` | `Property<String>` | Function name, ARN, or partial ARN |
| `zipFile` | `RegularFileProperty` | Path to the zip file to upload |
| `publish` | `Property<Boolean>` | Whether to publish a new version after update (defaults to `false`) |

### `BatchUpdateFunctionCode`

Updates the deployment package for multiple Lambda functions concurrently via coroutine `flatMapMerge`.
All functions registered via `registerArtifact` are uploaded in parallel. After each upload, the task
optionally waits for the function's `LastUpdateStatus` to reach `Successful` (controlled by `waitForActive`,
default `true`) before completing. When any function is published, the resulting version ARNs can be written
to a JSON file via `versionArnsFile`:

```kotlin
val updateAll = tasks.register<BatchUpdateFunctionCode>("updateAll") {
    service.set(lambda)
    registerArtifact("api") {
        functionName.set("my-api-fn")
        zipFile.set(layout.buildDirectory.file("dist/api.zip"))
        publish.set(true)
    }
    registerArtifact("worker") {
        functionName.set("my-worker-fn")
        zipFile.set(layout.buildDirectory.file("dist/worker.zip"))
        publish.set(true)
    }
    // waitForActive.set(false)  // opt out of post-update Active state wait
    versionArnsFile.set(layout.buildDirectory.file("lambda/version-arns.json"))
}
```

Wire the version ARNs file to a downstream task without forcing evaluation:

```kotlin
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
| `service` | `Property<LambdaClientBuildService>` | Build service supplying the Lambda client |
| `waitForActive` | `Property<Boolean>` | Wait for `LastUpdateStatus = Successful` after each upload (default `true`) |
| `versionArnsFile` | `RegularFileProperty` | Optional file to write published version ARN JSON into |

For BYO-client usage (no build service), extend `AbstractBatchUpdateFunctionCode` and set `client` directly.

## Why no WorkActions

The AWS Kotlin SDK exposes all service calls as `suspend` functions. A `WorkAction` that wraps a single suspend call reduces to:

```kotlin
override fun execute() {
    runBlocking { singleSuspendCall() }
}
```

This adds ceremony with no benefit: no return values, no isolation beyond what coroutines already provide, and no concurrency advantage (Gradle's task graph handles cross-task concurrency; coroutines handle within-task concurrency). WorkActions were designed for blocking Java SDK calls to avoid tying up Gradle's worker thread pool — that problem doesn't exist with a coroutine-based SDK.

Accordingly, this component exposes `DefaultTask` subclasses instead. Plugin authors needing compound operations should compose via Gradle task dependencies (sequential) or call `service.get().getClient()` directly inside a `runBlocking { coroutineScope { } }` block (parallel).

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-lambda-java-base](../aws-lambda-java-base) — Java SDK variant
