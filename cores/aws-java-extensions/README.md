# AWS Java Extensions

A Gradle library providing config-cache-safe build service infrastructure and Gradle credential adapters for the AWS
SDK for Java.

## Dependency

This library is a transitive dependency of the AWS Java Base plugins (`aws-s3-java-base`, `aws-codeartifact-java-base`,
etc.). Direct use is only needed when building plugins that define new AWS Java client build services or client info
types.

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-java-extensions")
}
```

## `AwsBuildServiceParams`

Config-cache-safe `BuildServiceParameters` interface for AWS Java SDK client build services. All fields are
serializable primitives; use the extension functions below to configure them rather than setting fields directly.

| Property | Type | Description |
|---|---|---|
| `regionId` | `Property<String>` | AWS region identifier (e.g. `"us-east-1"`). Leave unset to delegate to `DefaultAwsRegionProviderChain`. |
| `credentialSource` | `Property<AwsCredentialSource>` | Which credentials provider to construct. Leave unset for `AnonymousCredentialsProvider`. |
| `accessKeyId` | `Property<String>` | Access key ID. Used when `credentialSource` is `STATIC`. |
| `secretAccessKey` | `Property<String>` | Secret access key. Used when `credentialSource` is `STATIC`. |
| `sessionToken` | `Property<String>` | Session token for temporary credentials. Optional; used when `credentialSource` is `STATIC`. |
| `credentialsProfile` | `Property<String>` | Named credentials profile. Used when `credentialSource` is `PROFILE`. |

### Extension functions

Configure an `AwsBuildServiceParams` instance atomically using one of these functions:

```kotlin
gradle.sharedServices.registerIfAbsent("s3", S3ClientBuildService::class) {
    parameters {
        regionId.set("us-east-1")
        defaultCredentials()               // DefaultCredentialsProvider
        // anonymous()                     // AnonymousCredentialsProvider (default)
        // staticCredentials(akid, secret) // AwsBasicCredentials
        // sessionCredentials(akid, secret, token) // AwsSessionCredentials
        // profileCredentials("my-profile")        // ProfileCredentialsProvider
    }
}
```

| Function | Credential result |
|---|---|
| `anonymous()` | `AnonymousCredentialsProvider` |
| `defaultCredentials()` | `DefaultCredentialsProvider` (env vars, `~/.aws/credentials`, EC2/ECS metadata, …) |
| `staticCredentials(accessKey, secretKey)` | `StaticCredentialsProvider` with `AwsBasicCredentials` |
| `sessionCredentials(accessKey, secretKey, token)` | `StaticCredentialsProvider` with `AwsSessionCredentials` |
| `profileCredentials(profile)` | `ProfileCredentialsProvider` for the named profile |
| `from(Provider<PasswordCredentials>)` | `StaticCredentialsProvider`; maps `username` → `accessKeyId`, `password` → `secretAccessKey` |
| `from(Provider<AwsCredentials>)` | `StaticCredentialsProvider`; maps Gradle `AwsCredentials` fields; uses session credentials when `sessionToken` is non-null |

## `AbstractAwsJavaClientBuildService<C, P>`

Abstract base class for AWS Java SDK client build services. Extend this and implement `createClient()`, using
`configureBuilder()` to apply region and credentials from `AwsBuildServiceParams` to any standard AWS client builder:

```kotlin
abstract class SnsClientBuildService : AbstractAwsJavaClientBuildService<SnsClient, AwsBuildServiceParams>() {
    override fun createClient(): SnsClient = configureBuilder(SnsClient.builder()).build()
}
```

| Method | Description |
|---|---|
| `resolveRegion(): Region?` | Returns the `Region` from `regionId`, or `null` if unset. |
| `resolveCredentialsProvider(): AwsCredentialsProvider` | Constructs the credentials provider from `credentialSource` and its supporting fields. |
| `configureBuilder(builder: B): B` | Calls `resolveRegion()` and `resolveCredentialsProvider()` on any `AwsClientBuilder`, then returns it. |

## Credential Adapters

These classes adapt Gradle credential providers to `AwsCredentialsProvider` for use anywhere an
`AwsCredentialsProvider` is needed directly (outside of build service parameters).

### `GradleCredentialsProviders`

Adapts a `Provider<PasswordCredentials>` to an `AwsCredentialsProvider`. Maps `username` → `accessKeyId` and
`password` → `secretAccessKey`, producing `AwsBasicCredentials`.

```kotlin
class GradleCredentialsProviders(credentials: Provider<PasswordCredentials>) : AwsCredentialsProvider
```

### `GradleSessionCredentialsProvider`

Adapts a `Provider<org.gradle.api.credentials.AwsCredentials>` to an `AwsCredentialsProvider`. Maps `accessKey`,
`secretKey`, and `sessionToken`, producing `AwsSessionCredentials`.

```kotlin
class GradleSessionCredentialsProvider(credentials: Provider<AwsCredentials>) : AwsCredentialsProvider
```

## `AwsClientInfo<T>`

Base interface for AWS Java SDK client registrations in the `ClientsBaseService` container. Extends
`ServiceClientInfo<T>` where `T : AwsClient`.

| Property | Type | Description |
|---|---|---|
| `region` | `Property<Region>` | AWS region. Leave unset to use `DefaultAwsRegionProviderChain`. |
| `credentials` | `Property<AwsCredentialsProvider>` | Credentials provider. If absent, uses `AnonymousCredentialsProvider`. |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [AWS SDK for Java v2](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/)
- [Gradle credential handling](https://docs.gradle.org/current/userguide/declaring_repositories.html#sec:handling_credentials)
