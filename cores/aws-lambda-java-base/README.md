# AWS Lambda Java Base

A Kotlin library providing managed AWS Lambda client integration using the AWS SDK for Java, built on
`clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-lambda-java-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `LambdaClientBuildService` | `LambdaClient` (AWS SDK for Java) |

Register the build service from a plugin or `build.gradle.kts`:

```kotlin
val lambda = gradle.sharedServices.registerIfAbsent("lambda", LambdaClientBuildService::class) {
    parameters.region.set(Region.US_EAST_1)
    parameters.credentials.set(DefaultCredentialsProvider.create())
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

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-lambda-kotlin-base](../aws-lambda-kotlin-base) — Kotlin SDK variant
