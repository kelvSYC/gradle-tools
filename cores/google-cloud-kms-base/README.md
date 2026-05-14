# google-cloud-kms-base

Gradle library providing `BuildService`, `ValueSource`, and `WorkAction` primitives for
[Google Cloud KMS](https://cloud.google.com/kms/docs).

## Setup

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/kelvSYC/gradle-tools")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.kelvsyc.gradle:google-cloud-kms-base:<version>")
}
```

## Usage

### Register the build service

```kotlin
val kmsService = gradle.sharedServices.registerIfAbsent("kms", KmsClientBuildService::class) {
    parameters {
        applicationDefault()       // or serviceAccount(...), accessToken(...), etc.
    }
}
```

### Encrypt a file (symmetric)

```kotlin
workerExecutor.noIsolation().submit(EncryptAction::class) {
    service.set(kmsService)
    cryptoKeyName.set("projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/my-key")
    plaintextFile.set(layout.projectDirectory.file("secret.txt"))
    ciphertextFile.set(layout.buildDirectory.file("secret.enc"))
}
```

### Decrypt a file (symmetric)

```kotlin
workerExecutor.noIsolation().submit(DecryptAction::class) {
    service.set(kmsService)
    cryptoKeyName.set("projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/my-key")
    ciphertextFile.set(layout.projectDirectory.file("secret.enc"))
    plaintextFile.set(layout.buildDirectory.file("secret.txt"))
}
```

### Sign a file (asymmetric)

```kotlin
workerExecutor.noIsolation().submit(AsymmetricSignAction::class) {
    service.set(kmsService)
    cryptoKeyVersionName.set("projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/signing-key/cryptoKeyVersions/1")
    digestAlgorithm.set("SHA256")
    dataFile.set(layout.buildDirectory.file("app.jar"))
    signatureFile.set(layout.buildDirectory.file("app.jar.sig"))
}
```

### Decrypt a file (asymmetric)

```kotlin
workerExecutor.noIsolation().submit(AsymmetricDecryptAction::class) {
    service.set(kmsService)
    cryptoKeyVersionName.set("projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/rsa-key/cryptoKeyVersions/1")
    ciphertextFile.set(layout.projectDirectory.file("wrapped-key.enc"))
    plaintextFile.set(layout.buildDirectory.file("wrapped-key.bin"))
}
```

### Compute a MAC over a file

```kotlin
workerExecutor.noIsolation().submit(MacSignAction::class) {
    service.set(kmsService)
    cryptoKeyVersionName.set("projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/hmac-key/cryptoKeyVersions/1")
    dataFile.set(layout.buildDirectory.file("artifact.zip"))
    macFile.set(layout.buildDirectory.file("artifact.zip.mac"))
}
```

### List key rings

```kotlin
val keyRings: Provider<List<String>> = providers.of(ListKeyRingsValueSource::class) {
    parameters {
        service.set(kmsService)
        projectId.set("my-project")
        location.set("global")
    }
}
```

### Get a public key (asymmetric keys)

```kotlin
val publicKeyPem: Provider<String> = providers.of(GetPublicKeyValueSource::class) {
    parameters {
        service.set(kmsService)
        cryptoKeyVersionName.set("projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/signing-key/cryptoKeyVersions/1")
    }
}
```

## ValueSources

| Class | Parameters | Returns |
|-------|-----------|---------|
| `ListKeyRingsValueSource` | `service`, `projectId`, `location` | `List<String>` of KeyRing resource names |
| `ListCryptoKeysValueSource` | `service`, `projectId`, `location`, `keyRingId` | `List<String>` of CryptoKey resource names |
| `ListCryptoKeyVersionsValueSource` | `service`, `projectId`, `location`, `keyRingId`, `cryptoKeyId` | `List<String>` of CryptoKeyVersion resource names |
| `GetCryptoKeyValueSource` | `service`, `cryptoKeyName` | `String?` — canonical resource name |
| `GetPublicKeyValueSource` | `service`, `cryptoKeyVersionName` | `String?` — PEM-encoded public key |

## WorkActions

| Class | Parameters | File I/O |
|-------|-----------|---------|
| `EncryptAction` | `service`, `cryptoKeyName` | `plaintextFile` → `ciphertextFile` |
| `DecryptAction` | `service`, `cryptoKeyName` | `ciphertextFile` → `plaintextFile` |
| `AsymmetricSignAction` | `service`, `cryptoKeyVersionName`, `digestAlgorithm` | `dataFile` → `signatureFile` |
| `AsymmetricDecryptAction` | `service`, `cryptoKeyVersionName` | `ciphertextFile` → `plaintextFile` |
| `MacSignAction` | `service`, `cryptoKeyVersionName` | `dataFile` → `macFile` |
