# AWS STS Java Base

A Kotlin library providing a managed AWS Security Token Service (STS) client integration using the AWS SDK for
Java, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-sts-java-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `StsClientBuildService` | `StsClient` (AWS SDK for Java) |

Register the build service from a plugin or `build.gradle.kts`:

```kotlin
val sts = gradle.sharedServices.registerIfAbsent("sts", StsClientBuildService::class) {
    parameters.region.set(Region.US_EAST_1)
    parameters.credentials.set(DefaultCredentialsProvider.create())
}
```

Leave `region` unset to fall back to `DefaultAwsRegionProviderChain`. Leave `credentials` unset to fall back to
`AnonymousCredentialsProvider`.

> [!NOTE]
> For assume-role use cases, configure the AWS SDK's `StsAssumeRoleCredentialsProvider` and use it as the
> `credentials` of the *target* client (e.g. an `S3ClientInfo`) rather than going through this plugin. Exposing
> raw temporary credentials via a `ValueSource` is not supported, since `ValueSource` results may be cached or
> logged.

## Value Sources

### `GetCallerIdentityValueSource`

Returns the calling identity for the configured client as a `Map<String, String>` with the keys `account`,
`arn`, and `userId`. Useful for build-time diagnostics or for asserting the build is running under the expected
AWS principal:

```kotlin
val identity: Provider<Map<String, String>> = providers.of(GetCallerIdentityValueSource::class) {
    parameters {
        service.set(sts)
    }
}
val arn: Provider<String> = identity.map { it.getValue("arn") }
```

### `DecodeAuthorizationMessageValueSource`

Decodes an STS-encoded authorization failure message into a JSON document describing the policy evaluation that
produced the failure. Useful for surfacing IAM denial details in build logs:

```kotlin
val decoded: Provider<String> = providers.of(DecodeAuthorizationMessageValueSource::class) {
    parameters {
        service.set(sts)
        encodedMessage.set("…encoded blob from a previous denial…")
    }
}
```

Returns `null` and logs a warning if the call throws `StsException` (typically because the message has expired
or the caller lacks `sts:DecodeAuthorizationMessage`).

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-sts-kotlin-base](../aws-sts-kotlin-base) — Kotlin SDK variant
