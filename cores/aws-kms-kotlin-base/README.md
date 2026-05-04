# AWS KMS Kotlin Base

A Gradle plugin providing managed AWS Key Management Service (KMS) client integration using the AWS SDK for
Kotlin.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.aws-kms-kotlin-base")
}
```

## Client Type

One client info type is registered:

| Client info type | Client type |
|---|---|
| `KmsClientInfo` | `KmsClient` (AWS SDK for Kotlin) |

`KmsClientInfo` extends `AwsClientInfo` from `aws-kotlin-extensions`. Register a client:

```kotlin
serviceClients.service.get().registerIfAbsent<KmsClientInfo>("kms") {
    region.set("us-east-1")
    credentials.set(providers.credentials(AwsCredentials::class.java, "kms").asCredentialsProvider)
}
```

## Value Sources

### `DescribeKeyValueSource`

Retrieves the canonical ARN of a KMS key from its ID, ARN, or alias name:

```kotlin
val keyArn: Provider<String> = providers.of(DescribeKeyValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("kms")
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
        service.set(serviceClients.service)
        clientName.set("kms")
    }
}
```

## WorkActions

### `EncryptAction`

Encrypts the contents of a plaintext file under a KMS key and writes the resulting ciphertext blob to disk:

```kotlin
workerExecutor.noIsolation().submit(EncryptAction::class) {
    service.set(serviceClients.service)
    clientName.set("kms")
    keyId.set("alias/my-key")
    plaintextFile.set(layout.projectDirectory.file("secrets/config.json"))
    ciphertextFile.set(layout.buildDirectory.file("encrypted/config.json.kms"))
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of a `KmsClientInfo` |
| `keyId` | `Property<String>` | Key ID, ARN, or alias name to encrypt under |
| `plaintextFile` | `RegularFileProperty` | Plaintext input file |
| `ciphertextFile` | `RegularFileProperty` | Ciphertext output file |

### `DecryptAction`

Decrypts a KMS ciphertext blob back into plaintext:

```kotlin
workerExecutor.noIsolation().submit(DecryptAction::class) {
    service.set(serviceClients.service)
    clientName.set("kms")
    ciphertextFile.set(layout.projectDirectory.file("encrypted/config.json.kms"))
    plaintextFile.set(layout.buildDirectory.file("decrypted/config.json"))
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of a `KmsClientInfo` |
| `keyId` | `Property<String>` | Optional; required only for asymmetric keys |
| `ciphertextFile` | `RegularFileProperty` | Ciphertext input file |
| `plaintextFile` | `RegularFileProperty` | Plaintext output file |

For symmetric keys the key is determined from the ciphertext blob itself.

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-kotlin-extensions](../aws-kotlin-extensions) — `AwsClientInfo` base interface and credential adapters
- [aws-kms-java-base](../aws-kms-java-base) — Java SDK variant
