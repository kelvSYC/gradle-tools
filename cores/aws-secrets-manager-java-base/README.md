# AWS Secrets Manager Java Base

A Gradle plugin providing managed AWS Secrets Manager client integration using the AWS SDK for Java.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.aws-secrets-manager-java-base")
}
```

## Client Types

Two client info types are registered (a `SecretCacheClientInfo` binding must be registered separately — see below):

| Client info type | Client type | Use case |
|---|---|---|
| `SecretsManagerClientInfo` | `SecretsManagerClient` | Synchronous Secrets Manager operations |
| `SecretsManagerAsyncClientInfo` | `SecretsManagerAsyncClient` | Asynchronous Secrets Manager operations |

Both extend `AwsClientInfo` from `aws-java-extensions`. Register clients:

```kotlin
serviceClients.service.get().registerIfAbsent<SecretsManagerClientInfo>("secretsManager") {
    region.set(Region.US_EAST_1)
    credentials.set(DefaultCredentialsProvider.create())
}
```

### `SecretCacheClientInfo`

An optional client type backed by the AWS Secrets Manager caching library. It is not registered by the plugin
automatically — register its binding manually if needed:

```kotlin
serviceClients.service.get().registerBinding(SecretCacheClientInfo::class, SecretCacheClientInfoInternal::class)
serviceClients.service.get().registerIfAbsent<SecretCacheClientInfo>("secretsCache") {
    baseClient.set(serviceClients.getClient<SecretsManagerClient, SecretsManagerClientInfo>("secretsManager").get())
    maxCacheSize.set(1000)        // optional
    cacheItemTtl.set(3_600_000L) // optional, milliseconds
}
```

| Property | Type | Description |
|---|---|---|
| `baseClient` | `Property<SecretsManagerClient>` | The underlying sync client |
| `maxCacheSize` | `Property<Int>` | Maximum number of cached secrets |
| `cacheItemTtl` | `Property<Long>` | Cache TTL in milliseconds |

## Value Sources

### `SecretsManagerValueSource`

Retrieves a single string secret directly from Secrets Manager:

```kotlin
val secret: Provider<String> = providers.of(SecretsManagerValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("secretsManager")
        secretName.set("my/secret/name")
    }
}
```

Returns `null` and logs a warning if the call throws `SecretsManagerException`. Only string secrets are supported.

### `SecretFromCacheValueSource`

Retrieves a single string secret from the in-memory cache:

```kotlin
val secret: Provider<String> = providers.of(SecretFromCacheValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("secretsCache")
        secretName.set("my/secret/name")
    }
}
```

### `SecretBatchValueSource`

Retrieves multiple secrets in a single paginated batch call, returning a `Map<String, String>` keyed by secret name:

```kotlin
val secrets: Provider<Map<String, String>> = providers.of(SecretBatchValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("secretsManager")
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
- [aws-java-extensions](../aws-java-extensions) — `AwsClientInfo` base interface and credential adapters
- [aws-secrets-manager-kotlin-base](../aws-secrets-manager-kotlin-base) — Kotlin SDK variant
