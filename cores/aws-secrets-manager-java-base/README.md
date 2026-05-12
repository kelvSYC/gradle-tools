# AWS Secrets Manager Java Base

A Kotlin library providing managed AWS Secrets Manager client integration using the AWS SDK for Java, built on
`clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-secrets-manager-java-base")
}
```

## Build Services

| Class | Client type | Use case |
|---|---|---|
| `SecretsManagerClientBuildService` | `SecretsManagerClient` | Synchronous Secrets Manager operations |
| `SecretsManagerAsyncClientBuildService` | `SecretsManagerAsyncClient` | Asynchronous Secrets Manager operations |
| `SecretCacheBuildService` | `SecretCache` | In-memory cache backed by a `SecretsManagerClientBuildService` |

### Direct clients

```kotlin
val sm = gradle.sharedServices.registerIfAbsent("sm", SecretsManagerClientBuildService::class) {
    parameters.region.set(Region.US_EAST_1)
    parameters.credentials.set(DefaultCredentialsProvider.create())
}

val smAsync = gradle.sharedServices.registerIfAbsent("sm-async", SecretsManagerAsyncClientBuildService::class) {
    parameters.region.set(Region.US_EAST_1)
    parameters.credentials.set(DefaultCredentialsProvider.create())
}
```

Leave `region` unset to fall back to `DefaultAwsRegionProviderChain`. Leave `credentials` unset to fall back to
`AnonymousCredentialsProvider`.

### Secret cache (wraps a `SecretsManagerClient`)

```kotlin
val smCache = gradle.sharedServices.registerIfAbsent("sm-cache", SecretCacheBuildService::class) {
    parameters.baseService.set(sm)
    parameters.maxCacheSize.set(1000)       // optional
    parameters.cacheItemTtl.set(3_600_000L) // optional, milliseconds
}
```

`baseService` must be set to a registered `SecretsManagerClientBuildService`. The wrapped client is resolved
lazily — the underlying service is not instantiated until the cache itself is first accessed.

| Parameter | Type | Description |
|---|---|---|
| `baseService` | `Property<SecretsManagerClientBuildService>` | The build service supplying the underlying sync client |
| `maxCacheSize` | `Property<Int>` | Maximum number of cached secrets |
| `cacheItemTtl` | `Property<Long>` | Cache TTL in milliseconds |

## Value Sources

### `SecretsManagerValueSource`

Retrieves a single string secret directly from Secrets Manager:

```kotlin
val secret: Provider<String> = providers.of(SecretsManagerValueSource::class) {
    parameters {
        service.set(sm)
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
        service.set(smCache)
        secretName.set("my/secret/name")
    }
}
```

### `SecretBatchValueSource`

Retrieves multiple secrets in a single paginated batch call, returning a `Map<String, String>` keyed by secret
name:

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
- [aws-secrets-manager-kotlin-base](../aws-secrets-manager-kotlin-base) — Kotlin SDK variant
