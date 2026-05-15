# AWS KMS Java Base

A Kotlin library providing managed AWS Key Management Service (KMS) client integration using the AWS SDK for
Java, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-kms-java-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `KmsClientBuildService` | `KmsClient` (AWS SDK for Java) |

```kotlin
val kms = gradle.sharedServices.registerIfAbsent("kms", KmsClientBuildService::class) {
    parameters {
        regionId.set("us-east-1")
        defaultCredentials()
    }
}
```

Leave `region` unset to fall back to `DefaultAwsRegionProviderChain`. Leave `credentials` unset to fall back to
`AnonymousCredentialsProvider`.

## Value Sources

### `DescribeKeyValueSource`

Retrieves the canonical ARN of a KMS key from its ID, ARN, or alias name:

```kotlin
val keyArn: Provider<String> = providers.of(DescribeKeyValueSource::class) {
    parameters {
        service.set(kms)
        keyId.set("alias/my-key")
    }
}
```

Returns `null` and logs a warning if the call throws `KmsException`.

### `ListKeysValueSource`

Lists all KMS keys visible to the configured client, returning a `Map<String, String>` keyed by key ID with
the key ARN as the value. Pagination is handled internally:

```kotlin
val keys: Provider<Map<String, String>> = providers.of(ListKeysValueSource::class) {
    parameters {
        service.set(kms)
    }
}
```

## WorkActions

### `EncryptAction`

Encrypts the contents of a plaintext file under a KMS key and writes the resulting ciphertext blob to disk:

```kotlin
workerExecutor.noIsolation().submit(EncryptAction::class) {
    service.set(kms)
    keyId.set("alias/my-key")
    plaintextFile.set(layout.projectDirectory.file("secrets/config.json"))
    ciphertextFile.set(layout.buildDirectory.file("encrypted/config.json.kms"))
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<KmsClientBuildService>` | Build service supplying the KMS client |
| `keyId` | `Property<String>` | Key ID, ARN, or alias name to encrypt under |
| `plaintextFile` | `RegularFileProperty` | Plaintext input file |
| `ciphertextFile` | `RegularFileProperty` | Ciphertext output file |

### `DecryptAction`

Decrypts a KMS ciphertext blob back into plaintext:

```kotlin
workerExecutor.noIsolation().submit(DecryptAction::class) {
    service.set(kms)
    ciphertextFile.set(layout.projectDirectory.file("encrypted/config.json.kms"))
    plaintextFile.set(layout.buildDirectory.file("decrypted/config.json"))
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<KmsClientBuildService>` | Build service supplying the KMS client |
| `keyId` | `Property<String>` | Optional; required only for asymmetric keys |
| `ciphertextFile` | `RegularFileProperty` | Ciphertext input file |
| `plaintextFile` | `RegularFileProperty` | Plaintext output file |

For symmetric keys the key is determined from the ciphertext blob itself.

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-kms-kotlin-base](../aws-kms-kotlin-base) — Kotlin SDK variant
