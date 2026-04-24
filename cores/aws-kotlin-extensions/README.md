# AWS Kotlin Extensions

A Gradle library providing the base client info interface and Gradle credential adapters for the AWS SDK for Kotlin.

## Dependency

This library is a transitive dependency of the AWS Kotlin Base plugins (`aws-s3-kotlin-base`,
`aws-codeartifact-kotlin-base`, etc.). Direct use is only needed when building plugins that define new AWS Kotlin
client info types.

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-kotlin-extensions")
}
```

## `AwsClientInfo<T>`

Base interface for AWS Kotlin SDK client registrations. Extends `ServiceClientInfo<T>` where `T : SdkClient`.

| Property | Type | Description |
|---|---|---|
| `region` | `Property<String>` | AWS region string (e.g. `"us-east-1"`). Leave unset to use the SDK default chain. |
| `credentials` | `Property<CredentialsProvider>` | Credentials provider. Leave unset to use the SDK default chain. |

## Credential Extensions (`CredentialsProviderExtensions`)

### `Provider<AwsCredentials>.asCredentialsProvider`

Converts a `Provider<org.gradle.api.credentials.AwsCredentials>` to a `Provider<CredentialsProvider>`:

```kotlin
// Reads myClientAccessKey, myClientSecretKey, myClientSessionToken Gradle properties
val gradleCreds = providers.credentials(AwsCredentials::class.java, "myClient")
credentials.set(gradleCreds.asCredentialsProvider)
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

- [clients-base](../clients-base) — The underlying service client infrastructure
- [AWS SDK for Kotlin](https://docs.aws.amazon.com/sdk-for-kotlin/latest/developer-guide/)
- [Gradle credential handling](https://docs.gradle.org/current/userguide/declaring_repositories.html#sec:handling_credentials)
