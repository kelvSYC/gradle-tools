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
    parameters.region.set("us-east-1")
    parameters.credentials.set(providers.credentials(AwsCredentials::class.java, "sm").asCredentialsProvider)
}
```

## Value Sources

### `SecretsManagerValueSource`

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

### `SecretBatchValueSource`

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

## WorkActions

### `PutSecretValueAction`

Stores a new value in an existing Secrets Manager secret:

```kotlin
workerExecutor.noIsolation().submit(PutSecretValueAction::class) {
    service.set(sm)
    secretId.set("my/secret/name")
    secretString.set("{\"username\":\"admin\",\"password\":\"newPassword\"}")
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<SecretsManagerClientBuildService>` | Build service supplying the Secrets Manager client |
| `secretId` | `Property<String>` | Name or ARN of the secret to update |
| `secretString` | `Property<String>` | New secret value (string) |

Only string secrets are supported. The secret must already exist — use `CreateSecret` via the AWS CLI or console
to create new secrets.

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-secrets-manager-java-base](../aws-secrets-manager-java-base) — Java SDK variant with async client and secret cache support
