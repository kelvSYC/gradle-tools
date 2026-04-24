# AWS Java Extensions

A Gradle library providing the base client info interface and Gradle credential adapters for the AWS SDK for Java.

## Dependency

This library is a transitive dependency of the AWS Java Base plugins (`aws-s3-java-base`, `aws-codeartifact-java-base`,
etc.). Direct use is only needed when building plugins that define new AWS Java client info types.

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-java-extensions")
}
```

## `AwsClientInfo<T>`

Base interface for AWS Java SDK client registrations. Extends `ServiceClientInfo<T>` where `T : AwsClient`.

| Property | Type | Description |
|---|---|---|
| `region` | `Property<Region>` | AWS region. Leave unset to use `DefaultAwsRegionProviderChain`. |
| `credentials` | `Property<AwsCredentialsProvider>` | Credentials provider. If absent, uses `AnonymousCredentialsProvider`. |

Set `credentials` using the AWS SDK's standard providers:

```kotlin
// Default credential chain (environment, ~/.aws/credentials, instance profile, etc.)
credentials.set(DefaultCredentialsProvider.create())

// From Gradle PasswordCredentials (reads myClientUsername / myClientPassword)
val gradleCreds = providers.credentials(PasswordCredentials::class.java, "myClient")
credentials.set(GradleCredentialsProviders(gradleCreds))

// From Gradle AwsCredentials (reads accessKey, secretKey, sessionToken)
val gradleAwsCreds = providers.credentials(AwsCredentials::class.java, "myClient")
credentials.set(GradleSessionCredentialsProvider(gradleAwsCreds))
```

## Credential Adapters

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

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [AWS SDK for Java v2](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/)
- [Gradle credential handling](https://docs.gradle.org/current/userguide/declaring_repositories.html#sec:handling_credentials)
