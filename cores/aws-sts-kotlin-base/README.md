# AWS STS Kotlin Base

A Kotlin library providing a managed AWS Security Token Service (STS) client integration using the AWS SDK for
Kotlin, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-sts-kotlin-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `StsClientBuildService` | `StsClient` (AWS SDK for Kotlin) |

Register the build service from a plugin or `build.gradle.kts`:

```kotlin
val sts = gradle.sharedServices.registerIfAbsent("sts", StsClientBuildService::class) {
    parameters {
        region.set("us-east-1")
        from(providers.credentials(AwsCredentials::class.java, "sts"))
    }
}
```

Both `region` and the credentials extension call are optional. Leave `region` unset to use the AWS SDK for Kotlin
default region provider chain. Omit the credentials call to skip the `credentialsProvider` assignment, in which
case the SDK applies its own default behavior. See [aws-kotlin-extensions](../aws-kotlin-extensions) for the full
set of credential configuration functions.

> [!NOTE]
> For assume-role use cases, configure the AWS SDK's `StsAssumeRoleCredentialsProvider` and use it as the
> credentials provider of the *target* client (e.g. an `S3ClientBuildService`) rather than going through this
> plugin. Exposing raw temporary credentials via a `ValueSource` is not supported, since `ValueSource` results
> may be cached or logged.

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

## Task: `AbstractAssumeRole`

Assumes an AWS IAM role and produces temporary session credentials via the STS AssumeRole API.
Subclasses must implement `doExecute(credential: AwsSessionCredential)` to consume the credential
at execution time.

The credential is valid only within the `doExecute` call and must be used to construct a
short-lived AWS SDK client. Storing the credential in any Gradle property or input violates
configuration cache security guarantees — see [AwsSessionCredential](../aws-kotlin-extensions) for details.

```kotlin
abstract class MyAssumeRole : AbstractAssumeRole() {
    override fun doExecute(credential: AwsSessionCredential) {
        KmsClient {
            credentialsProvider = StaticCredentialsProvider {
                accessKeyId = credential.accessKeyId
                secretAccessKey = credential.secretAccessKey
                sessionToken = credential.sessionToken
            }
        }.use { client ->
            client.describeKey(DescribeKeyRequest { keyId = "arn:aws:kms:us-east-1:..." })
        }
    }
}

tasks.register<MyAssumeRole>("assumeRole") {
    service.set(sts)
    roleArn.set("arn:aws:iam::123456789012:role/MyRole")
    roleSessionName.set("my-build-session")
    duration.set(3600L)
}
```

| Property | Type | Description |
|---|---|---|
| `service` | `Property<StsClientBuildService>` | Build service supplying the STS client |
| `roleArn` | `Property<String>` | ARN of the IAM role to assume |
| `roleSessionName` | `Property<String>` | Session name for the assumed role |
| `duration` | `Property<Long>` | Session validity in seconds (900 to 3600) |
| `externalId` | `Property<String>` | Optional external ID required by the role's trust policy |

No explicit revocation is available for STS credentials — they self-expire at `AwsSessionCredential.expiration`.

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
- [aws-sts-java-base](../aws-sts-java-base) — Java SDK variant
