# google-cloud-kms-base Design

**Date:** 2026-05-13

## Context

No Google Cloud KMS component exists in this project. AWS KMS is covered by `aws-kms-java-base` and `aws-kms-kotlin-base` (encrypt, decrypt, list keys, describe key). GCP KMS is a single SDK with a single client, so one component suffices — no Java/Kotlin split. This component establishes parity with the AWS bases and adds GCP-distinguishing operations (asymmetric sign/decrypt, MAC sign) that the AWS components do not yet implement.

## Architecture

**Component:** `cores/google-cloud-kms-base`
**Package:** `com.kelvsyc.gradle.google.cloud.kms`
**Group/artifact:** `com.kelvsyc.gradle:google-cloud-kms-base`

**Library:** `com.google.cloud:google-cloud-kms` — already included in `google-cloud-libraries-bom`; add a no-version entry to `gradle/libs.versions.toml`.

**BuildService:** `KmsClientBuildService` extends `AbstractGcpClientBuildService<KeyManagementServiceClient, GcpBuildServiceParams>`. Implements `createClient()` using `KeyManagementServiceSettings.newBuilder()` + `resolveCredentialsProvider()`, identical in shape to `SecretManagerServiceClientBuildService`.

**Component wiring:** `settings.gradle.kts` follows the standard GCP pattern — includes `../../gradle/settings`, applies `com.kelvsyc.internal`, and includes `../clients-base` and `../google-cloud-extensions`.

## ValueSources

Resource-naming convention: list-scoped ValueSources take individual string components to specify the parent scope; get-style ValueSources take a single fully-qualified resource name string (composable with list output). All list operations use `iterateAll()` for automatic SDK-level pagination.

| Class | Parameters | Returns |
|---|---|---|
| `ListKeyRingsValueSource` | `service`, `projectId`, `location` | `List<String>` of KeyRing resource names |
| `ListCryptoKeysValueSource` | `service`, `projectId`, `location`, `keyRingId` | `List<String>` of CryptoKey resource names |
| `ListCryptoKeyVersionsValueSource` | `service`, `projectId`, `location`, `keyRingId`, `cryptoKeyId` | `List<String>` of CryptoKeyVersion resource names |
| `GetCryptoKeyValueSource` | `service`, `cryptoKeyName` (fully-qualified) | `String?` — canonical name; `null` + warning on `ApiException` |
| `GetPublicKeyValueSource` | `service`, `cryptoKeyVersionName` (fully-qualified) | `String?` — PEM-encoded public key; `null` + warning on `ApiException` |

## WorkActions

All WorkActions take a fully-qualified resource name string, directly composable with list ValueSource output.

| Class | Key parameter | File I/O |
|---|---|---|
| `EncryptAction` | `cryptoKeyName: Property<String>` | `plaintextFile` → `ciphertextFile` |
| `DecryptAction` | `cryptoKeyName: Property<String>` | `ciphertextFile` → `plaintextFile` |
| `AsymmetricSignAction` | `cryptoKeyVersionName: Property<String>` | `dataFile` → `signatureFile` |
| `AsymmetricDecryptAction` | `cryptoKeyVersionName: Property<String>` | `ciphertextFile` → `plaintextFile` |
| `MacSignAction` | `cryptoKeyVersionName: Property<String>` | `dataFile` → `macFile` |

`AsymmetricSignAction` also requires `digestAlgorithm: Property<String>` (`SHA256`, `SHA384`, `SHA512`) — the GCP KMS asymmetric sign API takes a pre-computed `Digest` proto, so the action hashes the file locally before the API call. This is necessary for large files (JARs, binaries) that exceed the 64 KB raw-data limit.

Key management mutations (CreateKeyRing, CreateCryptoKey, etc.) are out of scope — keys are pre-existing infrastructure, consistent with the pattern established by Secret Manager (`AddSecretVersionAction` exists; `CreateSecretAction` does not).

## Testing

**Unit tests:** Kotest + mockk. Mock `KmsClientBuildService.getClient()` to return a mock `KeyManagementServiceClient`. Verify the correct SDK method is called with the right resource name and byte content. Apply `--add-opens java.base/java.lang=ALL-UNNAMED` (required by all GCP components in this project).

**Integration tests:** Gradle TestKit via `gradle-testkit-jacoco`. Verify plugin wiring and task graph construction. No live GCP calls in CI.

## Out of Scope

- Key ring / crypto key creation actions
- `MacVerifyValueSource` — verification belongs inside a WorkAction body or a richer ValueSource, not as a standalone `Boolean`-returning primitive
- Raw encrypt/decrypt (`RawEncryptAction`, `RawDecryptAction`) — lower-level than needed for build tool use cases
