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
    parameters {
        regionId.set("us-east-1")
        defaultCredentials()
    }
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

## WorkActions

### `AbstractAssumeRoleWorkAction`

Assumes an IAM role via STS and passes temporary credentials to `doExecute`. This provides
an AWS-native alternative to Vault's `AbstractAwsCredentialWorkAction` when Vault is not available.

```kotlin
abstract class MyKmsAction : AbstractAssumeRoleWorkAction() {
    override fun doExecute(credential: AwsSessionCredential) {
        KmsClient.builder()
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsSessionCredentials.create(
                    credential.accessKeyId,
                    credential.secretAccessKey,
                    credential.sessionToken,
                )
            ))
            .build()
            .use { client -> /* perform KMS operation */ }
    }
}

workerExecutor.noIsolation().submit(MyKmsAction::class) {
    service.set(sts)
    roleArn.set("arn:aws:iam::123456789012:role/MyRole")
    roleSessionName.set("gradle-build")
    duration.set(3600L)
    // externalId.set("...") // optional, for cross-account trust
}
```

> **When to use this vs `StsAssumeRoleCredentialsProvider`:** If you only need to configure the
> assumed-role credentials on an existing `BuildService`-backed client, use
> `StsAssumeRoleCredentialsProvider` on that client's `AwsBuildServiceParams` directly —
> no `AbstractAssumeRoleWorkAction` is needed. Use this WorkAction when you need the raw
> temporary credentials at task execution time, for example to create a client dynamically or
> to pass credentials to a non-AWS API.

STS credentials self-expire at the end of the `duration` and have no explicit revocation API. The
credential must not escape `doExecute`: storing it in a WorkParameters property (even `@get:Internal`),
a task input, or a shared file writes it to `.gradle/configuration-cache/` in plaintext.

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-sts-kotlin-base](../aws-sts-kotlin-base) — Kotlin SDK variant
