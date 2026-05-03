# AWS Lambda Kotlin Base

A Gradle plugin providing managed AWS Lambda client integration using the AWS SDK for Kotlin.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.aws-lambda-kotlin-base")
}
```

## Client Type

One client info type is registered:

| Client info type | Client type |
|---|---|
| `LambdaClientInfo` | `LambdaClient` (AWS SDK for Kotlin) |

`LambdaClientInfo` extends `AwsClientInfo` from `aws-kotlin-extensions`. Register a client:

```kotlin
serviceClients.service.get().registerIfAbsent<LambdaClientInfo>("lambda") {
    region.set("us-east-1")
    credentials.set(providers.credentials(AwsCredentials::class.java, "lambda").asCredentialsProvider)
}
```

## Value Sources

### `GetFunctionConfigurationValueSource`

Retrieves a Lambda function's ARN (qualified by version or alias when set):

```kotlin
val functionArn: Provider<String> = providers.of(GetFunctionConfigurationValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("lambda")
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
        service.set(serviceClients.service)
        clientName.set("lambda")
    }
}
```

## WorkActions

### `InvokeFunctionAction`

Invokes a Lambda function (fire-and-forget — the response payload is discarded):

```kotlin
workerExecutor.noIsolation().submit(InvokeFunctionAction::class) {
    service.set(serviceClients.service)
    clientName.set("lambda")
    functionName.set("my-fn")
    qualifier.set("prod")          // optional
    payload.set("{\"hello\":\"world\"}") // optional UTF-8 payload
    invocationType.set("Event")     // optional: RequestResponse, Event, DryRun
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of a `LambdaClientInfo` |
| `functionName` | `Property<String>` | Function name, ARN, or partial ARN |
| `qualifier` | `Property<String>` | Optional version or alias |
| `payload` | `Property<String>` | Optional UTF-8 payload |
| `invocationType` | `Property<String>` | One of `RequestResponse`, `Event`, `DryRun` |

### `UpdateFunctionCodeAction`

Uploads a new deployment package zip to an existing Lambda function:

```kotlin
workerExecutor.noIsolation().submit(UpdateFunctionCodeAction::class) {
    service.set(serviceClients.service)
    clientName.set("lambda")
    functionName.set("my-fn")
    zipFile.set(layout.buildDirectory.file("dist/my-fn.zip"))
    publish.set(true)
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of a `LambdaClientInfo` |
| `functionName` | `Property<String>` | Function name, ARN, or partial ARN |
| `zipFile` | `RegularFileProperty` | Path to the zip file to upload |
| `publish` | `Property<Boolean>` | Whether to publish a new version after update (defaults to `false`) |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-kotlin-extensions](../aws-kotlin-extensions) — `AwsClientInfo` base interface and credential adapters
- [aws-lambda-java-base](../aws-lambda-java-base) — Java SDK variant
