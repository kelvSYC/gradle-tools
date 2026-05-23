# AWS Secrets Manager Kotlin Base

A Kotlin library providing managed AWS Secrets Manager client integration using the AWS SDK for Kotlin, built
on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-secrets-manager-kotlin-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `SecretsManagerClientBuildService` | `SecretsManagerClient` (AWS SDK for Kotlin) |

```kotlin
val sm = gradle.sharedServices.registerIfAbsent("sm", SecretsManagerClientBuildService::class) {
    parameters {
        region.set("us-east-1")
        defaultCredentials()
    }
}
```

Both `region` and the credentials extension call are optional. Leave `region` unset to use the AWS SDK for Kotlin
default region provider chain. Omit the credentials call to skip the `credentialsProvider` assignment, in which
case the SDK applies its own default behavior. See [aws-kotlin-extensions](../aws-kotlin-extensions) for the full
set of credential configuration functions.

## Value Sources

**Note on configuration cache safety:** Gradle serializes the result of every `ValueSource.obtain()` call to the configuration cache in plaintext. Any credential or secret value returned by a `ValueSource` will be stored in `.gradle/configuration-cache/` and is readable by any process with access to the build directory. The `ValueSource` implementations in this component that retrieve credentials or secrets are deprecated for this reason. Retrieve sensitive values inside a `WorkAction` at task execution time instead — the value is resolved after the cache has been read and is never written to it.

### **Deprecated.** `SecretsManagerValueSource`

Retrieves a single string secret from Secrets Manager:

```kotlin
val secret: Provider<String> = providers.of(SecretsManagerValueSource::class) {
    parameters {
        service.set(sm)
        secretName.set("my/secret/name")
    }
}
```

Only string secrets are supported. The `secretString` field of the response is returned directly.

### **Deprecated.** `SecretBatchValueSource`

Retrieves multiple secrets using the paginated batch API, returning a `Map<String, String>` keyed by secret name:

```kotlin
val secrets: Provider<Map<String, String>> = providers.of(SecretBatchValueSource::class) {
    parameters {
        service.set(sm)
        secretIds.addAll("secret/one", "secret/two")
    }
}
```

Only string secrets are supported.

## Tasks

### `PutSecretValue`

Stores a new value in an existing Secrets Manager secret:

```kotlin
tasks.register("updateSecret", PutSecretValue::class) {
    service.set(sm)
    secretId.set("my/secret/name")
    secretString.set("{\"username\":\"admin\",\"password\":\"newPassword\"}")
}
```

| Property | Type | Description |
|---|---|---|
| `service` | `Property<SecretsManagerClientBuildService>` | Build service supplying the Secrets Manager client |
| `secretId` | `Property<String>` | Name or ARN of the secret to update |
| `secretString` | `Property<String>` | New secret value (string) |

Only string secrets are supported. The secret must already exist — use `CreateSecret` via the AWS CLI or console
to create new secrets.

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
- [aws-secrets-manager-java-base](../aws-secrets-manager-java-base) — Java SDK variant with async client and secret cache support
