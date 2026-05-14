# AWS SSM Kotlin Base

A Kotlin library providing managed AWS Systems Manager (SSM) Parameter Store client integration using the AWS
SDK for Kotlin, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-ssm-kotlin-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `SsmClientBuildService` | `SsmClient` (AWS SDK for Kotlin) |

```kotlin
val ssm = gradle.sharedServices.registerIfAbsent("ssm", SsmClientBuildService::class) {
    parameters {
        region.set("us-east-1")
        from(providers.credentials(AwsCredentials::class.java, "ssm"))
    }
}
```

Both `region` and the credentials extension call are optional. Leave `region` unset to use the AWS SDK for Kotlin
default region provider chain. Omit the credentials call to skip the `credentialsProvider` assignment, in which
case the SDK applies its own default behavior. See [aws-kotlin-extensions](../aws-kotlin-extensions) for the full
set of credential configuration functions.

## Value Sources

### `GetParameterValueSource`

Retrieves a single parameter value from SSM Parameter Store:

```kotlin
val parameter: Provider<String> = providers.of(GetParameterValueSource::class) {
    parameters {
        service.set(ssm)
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
        service.set(ssm)
        path.set("/my/app/")
        recursive.set(true)
        withDecryption.set(true)
    }
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<SsmClientBuildService>` | Build service supplying the SSM client |
| `path` | `Property<String>` | Hierarchy path (e.g. `/my/app/`) |
| `recursive` | `Property<Boolean>` | Whether to recurse into sub-paths (defaults to `false`) |
| `withDecryption` | `Property<Boolean>` | Whether to decrypt `SecureString` values (defaults to `false`) |

## WorkActions

### `PutParameterAction`

Creates or updates a parameter in SSM Parameter Store:

```kotlin
workerExecutor.noIsolation().submit(PutParameterAction::class) {
    service.set(ssm)
    parameterName.set("/my/app/feature-flag")
    parameterValue.set("enabled")
    parameterType.set("String") // String, StringList, or SecureString
    overwrite.set(true)
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<SsmClientBuildService>` | Build service supplying the SSM client |
| `parameterName` | `Property<String>` | Name of the parameter to create or update |
| `parameterValue` | `Property<String>` | New parameter value |
| `parameterType` | `Property<String>` | One of `String`, `StringList`, `SecureString`; required when creating |
| `overwrite` | `Property<Boolean>` | Whether to overwrite an existing parameter (defaults to `false`) |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-ssm-java-base](../aws-ssm-java-base) — Java SDK variant
