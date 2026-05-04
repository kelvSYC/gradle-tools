# AWS STS Kotlin Base

A Gradle plugin providing managed AWS Security Token Service (STS) client integration using the AWS SDK for
Kotlin.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.aws-sts-kotlin-base")
}
```

## Client Type

One client info type is registered:

| Client info type | Client type |
|---|---|
| `StsClientInfo` | `StsClient` (AWS SDK for Kotlin) |

`StsClientInfo` extends `AwsClientInfo` from `aws-kotlin-extensions`. Register a client:

```kotlin
serviceClients.service.get().registerIfAbsent<StsClientInfo>("sts") {
    region.set("us-east-1")
    credentials.set(providers.credentials(AwsCredentials::class.java, "sts").asCredentialsProvider)
}
```

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
        service.set(serviceClients.service)
        clientName.set("sts")
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
        service.set(serviceClients.service)
        clientName.set("sts")
        encodedMessage.set("…encoded blob from a previous denial…")
    }
}
```

Returns `null` and logs a warning if the call throws `StsException` (typically because the message has expired
or the caller lacks `sts:DecodeAuthorizationMessage`).

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-kotlin-extensions](../aws-kotlin-extensions) — `AwsClientInfo` base interface and credential adapters
- [aws-sts-java-base](../aws-sts-java-base) — Java SDK variant
