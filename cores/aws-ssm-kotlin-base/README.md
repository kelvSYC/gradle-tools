# AWS SSM Kotlin Base

A Gradle plugin providing managed AWS Systems Manager (SSM) Parameter Store client integration using the AWS SDK
for Kotlin.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.aws-ssm-kotlin-base")
}
```

## Client Type

One client info type is registered:

| Client info type | Client type |
|---|---|
| `SsmClientInfo` | `SsmClient` (AWS SDK for Kotlin) |

`SsmClientInfo` extends `AwsClientInfo` from `aws-kotlin-extensions`. Register a client:

```kotlin
serviceClients.service.get().registerIfAbsent<SsmClientInfo>("ssm") {
    region.set("us-east-1")
    credentials.set(providers.credentials(AwsCredentials::class.java, "ssm").asCredentialsProvider)
}
```

## Value Sources

### `GetParameterValueSource`

Retrieves a single parameter value from SSM Parameter Store using `runBlocking`:

```kotlin
val parameter: Provider<String> = providers.of(GetParameterValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("ssm")
        parameterName.set("/my/app/db-host")
        withDecryption.set(true) // required for SecureString parameters
    }
}
```

Returns `null` and logs a warning if the call throws `SsmException`.

### `GetParametersByPathValueSource`

Retrieves all parameters under a hierarchy path using the paginated API, returning a `Map<String, String>` keyed
by parameter name:

```kotlin
val parameters: Provider<Map<String, String>> = providers.of(GetParametersByPathValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("ssm")
        path.set("/my/app/")
        recursive.set(true)
        withDecryption.set(true)
    }
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of an `SsmClientInfo` |
| `path` | `Property<String>` | Hierarchy path (e.g. `/my/app/`) |
| `recursive` | `Property<Boolean>` | Whether to recurse into sub-paths (defaults to `false`) |
| `withDecryption` | `Property<Boolean>` | Whether to decrypt `SecureString` values (defaults to `false`) |

## WorkActions

### `PutParameterAction`

Creates or updates a parameter in SSM Parameter Store:

```kotlin
workerExecutor.noIsolation().submit(PutParameterAction::class) {
    service.set(serviceClients.service)
    clientName.set("ssm")
    parameterName.set("/my/app/feature-flag")
    parameterValue.set("enabled")
    parameterType.set("String") // String, StringList, or SecureString
    overwrite.set(true)
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of an `SsmClientInfo` |
| `parameterName` | `Property<String>` | Name of the parameter to create or update |
| `parameterValue` | `Property<String>` | New parameter value |
| `parameterType` | `Property<String>` | One of `String`, `StringList`, `SecureString`; required when creating |
| `overwrite` | `Property<Boolean>` | Whether to overwrite an existing parameter (defaults to `false`) |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-kotlin-extensions](../aws-kotlin-extensions) — `AwsClientInfo` base interface and credential adapters
- [aws-ssm-java-base](../aws-ssm-java-base) — Java SDK variant
