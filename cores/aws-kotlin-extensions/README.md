# AWS Kotlin Extensions

A Gradle library providing config-cache-safe build service infrastructure and Gradle credential adapters for the AWS
SDK for Kotlin.

## Dependency

This library is a transitive dependency of the AWS Kotlin Base plugins (`aws-s3-kotlin-base`,
`aws-codeartifact-kotlin-base`, etc.). Direct use is only needed when building plugins that define new AWS Kotlin
client build services or client info types.

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-kotlin-extensions")
}
```

## `AwsBuildServiceParams`

Config-cache-safe `BuildServiceParameters` interface for AWS Kotlin SDK client build services. All fields are
serializable primitives; use the extension functions below to configure them rather than setting fields directly.

| Property | Type | Description |
|---|---|---|
| `region` | `Property<String>` | AWS region identifier (e.g. `"us-east-1"`). Leave unset to delegate region resolution to the AWS SDK for Kotlin's default region provider chain. |
| `credentialSource` | `Property<AwsCredentialSource>` | Which credentials provider to construct. Leave unset for anonymous mode (no `credentialsProvider` is assigned to the client). |
| `accessKeyId` | `Property<String>` | Access key ID. Used when `credentialSource` is `STATIC`. |
| `secretAccessKey` | `Property<String>` | Secret access key. Used when `credentialSource` is `STATIC`. |
| `sessionToken` | `Property<String>` | Session token for temporary credentials. Optional; used when `credentialSource` is `STATIC`. |
| `credentialsProfile` | `Property<String>` | Named credentials profile. Used when `credentialSource` is `PROFILE`. |

### Extension functions

Configure an `AwsBuildServiceParams` instance atomically using one of these functions:

```kotlin
gradle.sharedServices.registerIfAbsent("s3", S3ClientBuildService::class) {
    parameters {
        region.set("us-east-1")
        defaultCredentials()               // DefaultChainCredentialsProvider
        // anonymous()                     // No credentialsProvider assigned (default)
        // staticCredentials(akid, secret) // StaticCredentialsProvider with basic Credentials
        // sessionCredentials(akid, secret, token) // StaticCredentialsProvider with session Credentials
        // profileCredentials("my-profile")        // ProfileCredentialsProvider
    }
}
```

| Function | Credential result |
|---|---|
| `anonymous()` | No `credentialsProvider` is assigned; the AWS SDK for Kotlin falls back to its own default. |
| `defaultCredentials()` | `DefaultChainCredentialsProvider` (env vars, `~/.aws/credentials`, EC2/ECS metadata, …) |
| `staticCredentials(accessKey, secretKey)` | `StaticCredentialsProvider` with basic `Credentials` |
| `sessionCredentials(accessKey, secretKey, token)` | `StaticCredentialsProvider` with session `Credentials` |
| `profileCredentials(profile)` | `ProfileCredentialsProvider` for the named profile |
| `from(Provider<PasswordCredentials>)` | `StaticCredentialsProvider`; maps `username` → `accessKeyId`, `password` → `secretAccessKey` |
| `from(Provider<AwsCredentials>)` | `StaticCredentialsProvider`; maps Gradle `AwsCredentials` fields; uses session credentials when `sessionToken` is non-null |

## `AbstractAwsKotlinClientBuildService<C, P>`

Abstract base class for AWS Kotlin SDK client build services. Because the AWS Kotlin SDK exposes a service-specific
DSL builder per client (rather than a shared fluent builder type), subclasses apply region and credentials by
reading `resolveRegion()` and `resolveCredentialsProvider()` from inside their DSL block:

```kotlin
abstract class SqsClientBuildService :
    AbstractAwsKotlinClientBuildService<SqsClient, AwsBuildServiceParams>() {
    override fun createClient(): SqsClient = SqsClient {
        resolveRegion()?.let { region = it }
        resolveCredentialsProvider()?.let { credentialsProvider = it }
    }
}
```

| Method | Description |
|---|---|
| `resolveRegion(): String?` | Returns the region identifier from `region`, or `null` if unset. |
| `resolveCredentialsProvider(): CredentialsProvider?` | Constructs the credentials provider from `credentialSource` and its supporting fields. Returns `null` for `ANONYMOUS`/unset, in which case the caller should skip the `credentialsProvider` assignment. |

## Credential Extensions (`CredentialsProviderExtensions`)

These extensions are for cases that need a `CredentialsProvider` outside of `BuildServiceParameters` context.

### `Provider<AwsCredentials>.asCredentialsProvider`

Converts a `Provider<org.gradle.api.credentials.AwsCredentials>` to a `Provider<CredentialsProvider>`:

```kotlin
val gradleCreds = providers.credentials(AwsCredentials::class.java, "myClient")
val sdkCreds: Provider<CredentialsProvider> = gradleCreds.asCredentialsProvider
```

The resulting `CredentialsProvider` resolves to a `StaticCredentialsProvider` backed by the Gradle credentials.

### `Provider<Credentials>.asGradleCredentials`

Converts a `Provider<aws.smithy.kotlin.runtime.auth.awscredentials.Credentials>` to a
`Provider<org.gradle.api.credentials.AwsCredentials>`:

```kotlin
val sdkCreds: Provider<Credentials> = ...
val gradleCreds: Provider<AwsCredentials> = sdkCreds.asGradleCredentials
```

The returned `AwsCredentials` is immutable — calling any setter throws `UnsupportedOperationException`.

## See Also

- [AWS SDK for Kotlin](https://docs.aws.amazon.com/sdk-for-kotlin/latest/developer-guide/)
- [Gradle credential handling](https://docs.gradle.org/current/userguide/declaring_repositories.html#sec:handling_credentials)
