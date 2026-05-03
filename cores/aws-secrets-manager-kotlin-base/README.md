# AWS Secrets Manager Kotlin Base

A Gradle plugin providing managed AWS Secrets Manager client integration using the AWS SDK for Kotlin.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.aws-secrets-manager-kotlin-base")
}
```

## Client Type

One client info type is registered:

| Client info type | Client type |
|---|---|
| `SecretsManagerClientInfo` | `SecretsManagerClient` (AWS SDK for Kotlin) |

`SecretsManagerClientInfo` extends `AwsClientInfo` from `aws-kotlin-extensions`. Register a client:

```kotlin
serviceClients.service.get().registerIfAbsent<SecretsManagerClientInfo>("secretsManager") {
    region.set("us-east-1")
    credentials.set(providers.credentials(AwsCredentials::class.java, "secretsManager").asCredentialsProvider)
}
```

## Value Sources

### `SecretsManagerValueSource`

Retrieves a single string secret from Secrets Manager using `runBlocking`:

```kotlin
val secret: Provider<String> = providers.of(SecretsManagerValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("secretsManager")
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
        service.set(serviceClients.service)
        clientName.set("secretsManager")
        secretIds.addAll("secret/one", "secret/two")
    }
}
```

Only string secrets are supported. Secrets with a `null` name or `null` `secretString` will cause a
`NullPointerException` — ensure all requested secrets are string secrets.

## WorkActions

### `PutSecretValueAction`

Stores a new value in an existing Secrets Manager secret:

```kotlin
workerExecutor.noIsolation().submit(PutSecretValueAction::class) {
    service.set(serviceClients.service)
    clientName.set("secretsManager")
    secretId.set("my/secret/name")
    secretString.set("{\"username\":\"admin\",\"password\":\"newPassword\"}")
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of a `SecretsManagerClientInfo` |
| `secretId` | `Property<String>` | Name or ARN of the secret to update |
| `secretString` | `Property<String>` | New secret value (string) |

Only string secrets are supported. The secret must already exist — use `CreateSecret` via the AWS CLI or console
to create new secrets.

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-kotlin-extensions](../aws-kotlin-extensions) — `AwsClientInfo` base interface and credential adapters
- [aws-secrets-manager-java-base](../aws-secrets-manager-java-base) — Java SDK variant with async client and secret cache support
