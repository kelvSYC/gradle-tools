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

## WorkActions

### `InvokeFunctionAction`

Invokes a Lambda function (fire-and-forget — the response payload is discarded):

```kotlin
workerExecutor.noIsolation().submit(InvokeFunctionAction::class) {
    service.set(lambda)
    functionName.set("my-fn")
    qualifier.set("prod")          // optional
    payload.set("{\"hello\":\"world\"}") // optional UTF-8 payload
    invocationType.set("Event")     // optional: RequestResponse, Event, DryRun
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<LambdaClientBuildService>` | Build service supplying the Lambda client |
| `functionName` | `Property<String>` | Function name, ARN, or partial ARN |
| `qualifier` | `Property<String>` | Optional version or alias |
| `payload` | `Property<String>` | Optional UTF-8 payload |
| `invocationType` | `Property<String>` | One of `RequestResponse`, `Event`, `DryRun` |

### `UpdateFunctionCodeAction`

Uploads a new deployment package zip to an existing Lambda function:

```kotlin
workerExecutor.noIsolation().submit(UpdateFunctionCodeAction::class) {
    service.set(lambda)
    functionName.set("my-fn")
    zipFile.set(layout.buildDirectory.file("dist/my-fn.zip"))
    publish.set(true)
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<LambdaClientBuildService>` | Build service supplying the Lambda client |
| `functionName` | `Property<String>` | Function name, ARN, or partial ARN |
| `zipFile` | `RegularFileProperty` | Path to the zip file to upload |
| `publish` | `Property<Boolean>` | Whether to publish a new version after update (defaults to `false`) |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-lambda-java-base](../aws-lambda-java-base) — Java SDK variant
