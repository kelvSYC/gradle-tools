# AWS KMS Kotlin Base

A Kotlin library providing managed AWS Key Management Service (KMS) client integration using the AWS SDK for
Kotlin, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-kms-kotlin-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `KmsClientBuildService` | `KmsClient` (AWS SDK for Kotlin) |

```kotlin
val kms = gradle.sharedServices.registerIfAbsent("kms", KmsClientBuildService::class) {
    parameters {
        region.set("us-east-1")
        from(providers.credentials(AwsCredentials::class.java, "kms"))
    }
}
```

Both `region` and the credentials extension call are optional. Leave `region` unset to use the AWS SDK for Kotlin
default region provider chain. Omit the credentials call to skip the `credentialsProvider` assignment, in which
case the SDK applies its own default behavior. See [aws-kotlin-extensions](../aws-kotlin-extensions) for the full
set of credential configuration functions.

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
- [aws-kms-java-base](../aws-kms-java-base) — Java SDK variant
