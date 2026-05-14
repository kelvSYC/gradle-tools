# google-cloud-kms-base Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create `cores/google-cloud-kms-base`, a Gradle library component that exposes Google Cloud KMS symmetric encrypt/decrypt, asymmetric sign/decrypt, and MAC sign operations as Gradle `WorkAction` and `ValueSource` primitives backed by a `BuildService`-managed SDK client.

**Architecture:** A single `KmsClientBuildService` manages the `KeyManagementServiceClient` lifecycle via the existing `AbstractGcpClientBuildService` base class. Five `ValueSource` implementations cover key enumeration and metadata. Five `WorkAction` implementations cover cryptographic operations with file I/O. No task types; this is a library component.

**Tech Stack:** Kotlin, Gradle (kotlin-gradle-library convention), Google Cloud KMS Java SDK (`com.google.cloud:google-cloud-kms`), Kotest (FunSpec), mockk, google-cloud-extensions (credential resolution), clients-base (client lifecycle).

**Spec:** `docs/superpowers/specs/2026-05-13-google-cloud-kms-base-design.md`

---

## File Map

**New component root:** `cores/google-cloud-kms-base/`

| File | Purpose |
|------|---------|
| `settings.gradle.kts` | Component settings, includes `clients-base` + `google-cloud-extensions` |
| `gradle.properties` | Dokka V2 migration flag |
| `build.gradle.kts` | Dependencies, convention plugins, Dokka module name |
| `README.md` | Public-facing usage docs |
| `src/main/kotlin/.../KmsClientBuildService.kt` | BuildService wrapping `KeyManagementServiceClient` |
| `src/main/kotlin/.../ListKeyRingsValueSource.kt` | ValueSource listing KeyRings by project + location |
| `src/main/kotlin/.../ListCryptoKeysValueSource.kt` | ValueSource listing CryptoKeys by KeyRing |
| `src/main/kotlin/.../ListCryptoKeyVersionsValueSource.kt` | ValueSource listing CryptoKeyVersions by CryptoKey |
| `src/main/kotlin/.../GetCryptoKeyValueSource.kt` | ValueSource resolving a fully-qualified CryptoKey name |
| `src/main/kotlin/.../GetPublicKeyValueSource.kt` | ValueSource returning PEM public key for an asymmetric version |
| `src/main/kotlin/.../EncryptAction.kt` | WorkAction: symmetric encrypt file |
| `src/main/kotlin/.../DecryptAction.kt` | WorkAction: symmetric decrypt file |
| `src/main/kotlin/.../AsymmetricSignAction.kt` | WorkAction: asymmetric sign file (pre-computed digest) |
| `src/main/kotlin/.../AsymmetricDecryptAction.kt` | WorkAction: asymmetric decrypt file |
| `src/main/kotlin/.../MacSignAction.kt` | WorkAction: compute HMAC over file |
| `src/test/kotlin/.../MockKmsClientBuildService.kt` | Test fixture: swap `createClient()` for a pre-set mock |
| `src/test/kotlin/.../ProviderFactoryExtensions.kt` | Test fixture: `ofKt` workaround for kotlin-gradle-library DSL gap |
| `src/test/kotlin/.../KmsClientBuildServiceSpec.kt` | Unit tests for BuildService |
| `src/test/kotlin/.../ListKeyRingsValueSourceSpec.kt` | Unit tests |
| `src/test/kotlin/.../ListCryptoKeysValueSourceSpec.kt` | Unit tests |
| `src/test/kotlin/.../ListCryptoKeyVersionsValueSourceSpec.kt` | Unit tests |
| `src/test/kotlin/.../GetCryptoKeyValueSourceSpec.kt` | Unit tests |
| `src/test/kotlin/.../GetPublicKeyValueSourceSpec.kt` | Unit tests |
| `src/test/kotlin/.../EncryptActionSpec.kt` | Unit tests |
| `src/test/kotlin/.../DecryptActionSpec.kt` | Unit tests |
| `src/test/kotlin/.../AsymmetricSignActionSpec.kt` | Unit tests |
| `src/test/kotlin/.../AsymmetricDecryptActionSpec.kt` | Unit tests |
| `src/test/kotlin/.../MacSignActionSpec.kt` | Unit tests |

**Modified files:**
- `gradle/libs.versions.toml` — add `google-cloud-kms` library entry
- `README.md` (root) — add `google-cloud-kms-base` row to the "Other bases" table

All source paths use the package `com.kelvsyc.gradle.google.cloud.kms`.

---

## Task 1: Component scaffold

**Files:**
- Create: `cores/google-cloud-kms-base/settings.gradle.kts`
- Create: `cores/google-cloud-kms-base/gradle.properties`
- Create: `cores/google-cloud-kms-base/build.gradle.kts`
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Add `google-cloud-kms` to the version catalog**

In `gradle/libs.versions.toml`, under the `# API Dependencies` section, alongside the other `google-cloud-*` entries, add:

```toml
google-cloud-kms = { module = "com.google.cloud:google-cloud-kms" } # version from BOM 'google-cloud-libraries-bom'
```

- [ ] **Step 2: Create `cores/google-cloud-kms-base/settings.gradle.kts`**

```kotlin
pluginManagement {
    includeBuild("../../gradle/settings")
}

plugins {
    id("com.kelvsyc.internal")
}

includeBuild("../clients-base")
includeBuild("../gradle-extensions")
includeBuild("../google-cloud-extensions")
```

- [ ] **Step 3: Create `cores/google-cloud-kms-base/gradle.properties`**

```properties
# Dokka V2 migration
org.jetbrains.dokka.experimental.gradle.pluginMode=V2EnabledWithHelpers
```

- [ ] **Step 4: Create `cores/google-cloud-kms-base/build.gradle.kts`**

```kotlin
import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-gradle-library")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Google Cloud KMS Base")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")
    api("com.kelvsyc.gradle:google-cloud-extensions")
    implementation("com.kelvsyc.gradle:gradle-extensions")

    api(libs.google.auth.library.credentials)
    api(libs.google.cloud.kms)
    api(libs.google.gax)
    implementation(libs.google.protobuf.java)

    testImplementation(libs.mockk)
}

tasks.test {
    // FIXME https://github.com/gradle/gradle/issues/18647
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}
```

- [ ] **Step 5: Verify the component is picked up by the composite build**

```bash
./gradlew :google-cloud-kms-base:dependencies --configuration compileClasspath
```

Expected: dependency tree printed without errors, showing `google-cloud-kms`, `clients-base`, and `google-cloud-extensions`.

- [ ] **Step 6: Commit**

```bash
git add gradle/libs.versions.toml cores/google-cloud-kms-base/
git commit -m "chore: scaffold google-cloud-kms-base component"
```

---

## Task 2: `KmsClientBuildService`

**Files:**
- Create: `cores/google-cloud-kms-base/src/main/kotlin/com/kelvsyc/gradle/google/cloud/kms/KmsClientBuildService.kt`
- Create: `cores/google-cloud-kms-base/src/test/kotlin/com/kelvsyc/gradle/google/cloud/kms/MockKmsClientBuildService.kt`
- Create: `cores/google-cloud-kms-base/src/test/kotlin/com/kelvsyc/gradle/google/cloud/kms/ProviderFactoryExtensions.kt`
- Create: `cores/google-cloud-kms-base/src/test/kotlin/com/kelvsyc/gradle/google/cloud/kms/KmsClientBuildServiceSpec.kt`

- [ ] **Step 1: Write the failing test**

Create `src/test/kotlin/com/kelvsyc/gradle/google/cloud/kms/KmsClientBuildServiceSpec.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.KeyManagementServiceClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class KmsClientBuildServiceSpec : FunSpec() {
    init {
        test("getClient - returns the mock client") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}
                .get()

            service.getClient() shouldBe client
        }
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
./gradlew :google-cloud-kms-base:test --tests "*.KmsClientBuildServiceSpec"
```

Expected: compilation failure — `MockKmsClientBuildService` and `KmsClientBuildService` do not exist yet.

- [ ] **Step 3: Create `KmsClientBuildService`**

Create `src/main/kotlin/com/kelvsyc/gradle/google/cloud/kms/KmsClientBuildService.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.cloud.kms.v1.KeyManagementServiceSettings
import com.kelvsyc.gradle.google.cloud.AbstractGcpClientBuildService
import com.kelvsyc.gradle.google.cloud.GcpBuildServiceParams

/**
 * Build service managing a [KeyManagementServiceClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring the credential source via the extension functions on [GcpBuildServiceParams] (e.g.
 * [applicationDefault][com.kelvsyc.gradle.google.cloud.applicationDefault],
 * [serviceAccount][com.kelvsyc.gradle.google.cloud.serviceAccount]). The same registration can
 * then be shared with value sources and work actions via a
 * `Property<KmsClientBuildService>` parameter.
 */
abstract class KmsClientBuildService :
    AbstractGcpClientBuildService<KeyManagementServiceClient, GcpBuildServiceParams>() {

    override fun createClient(): KeyManagementServiceClient {
        val settings = KeyManagementServiceSettings.newBuilder().apply {
            resolveCredentialsProvider()?.let { credentialsProvider = it }
        }.build()
        return KeyManagementServiceClient.create(settings)
    }
}
```

- [ ] **Step 4: Create the test-only mock and `ofKt` helper**

Create `src/test/kotlin/com/kelvsyc/gradle/google/cloud/kms/MockKmsClientBuildService.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.KeyManagementServiceClient

/**
 * Test-only [KmsClientBuildService] that returns a pre-supplied mock client.
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockKmsClientBuildService : KmsClientBuildService() {
    override fun createClient(): KeyManagementServiceClient =
        checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: KeyManagementServiceClient? = null
    }
}
```

Create `src/test/kotlin/com/kelvsyc/gradle/google/cloud/kms/ProviderFactoryExtensions.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.provider.ValueSourceSpec
import org.gradle.kotlin.dsl.of
import kotlin.reflect.KClass

// Workaround: under the kotlin-gradle-library convention, Kotlin does not treat Gradle's
// Action<in T> as T.() -> Unit, so providers.of(...) { parameters.X } fails to resolve.
internal fun <T : Any, P : ValueSourceParameters> ProviderFactory.ofKt(
    valueSourceType: KClass<out ValueSource<T, P>>,
    configuration: ValueSourceSpec<P>.() -> Unit
) = of(valueSourceType, configuration)
```

- [ ] **Step 5: Run the test to verify it passes**

```bash
./gradlew :google-cloud-kms-base:test --tests "*.KmsClientBuildServiceSpec"
```

Expected: `BUILD SUCCESSFUL`, 1 test passed.

- [ ] **Step 6: Commit**

```bash
git add cores/google-cloud-kms-base/src/
git commit -m "feat(google-cloud-kms-base): add KmsClientBuildService"
```

---

## Task 3: List ValueSources

**Files:**
- Create: `src/main/kotlin/.../ListKeyRingsValueSource.kt`
- Create: `src/main/kotlin/.../ListCryptoKeysValueSource.kt`
- Create: `src/main/kotlin/.../ListCryptoKeyVersionsValueSource.kt`
- Create: `src/test/kotlin/.../ListKeyRingsValueSourceSpec.kt`
- Create: `src/test/kotlin/.../ListCryptoKeysValueSourceSpec.kt`
- Create: `src/test/kotlin/.../ListCryptoKeyVersionsValueSourceSpec.kt`

All paths under `cores/google-cloud-kms-base/`.

- [ ] **Step 1: Write failing tests for all three list ValueSources**

Create `src/test/kotlin/com/kelvsyc/gradle/google/cloud/kms/ListKeyRingsValueSourceSpec.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.cloud.kms.v1.KeyRing
import com.google.cloud.kms.v1.ListKeyRingsRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListKeyRingsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns key ring resource names from paginated response") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val rings = listOf(
                KeyRing.newBuilder().setName("projects/my-project/locations/global/keyRings/ring-a").build(),
                KeyRing.newBuilder().setName("projects/my-project/locations/global/keyRings/ring-b").build(),
            )
            val paged = mockk<KeyManagementServiceClient.ListKeyRingsPagedResponse>()
            every { paged.iterateAll() } returns rings

            val slot = slot<ListKeyRingsRequest>()
            every { client.listKeyRings(capture(slot)) } returns paged

            val provider = project.providers.ofKt(ListKeyRingsValueSource::class) {
                parameters.service.set(service)
                parameters.projectId.set("my-project")
                parameters.location.set("global")
            }

            provider.get() shouldBe listOf(
                "projects/my-project/locations/global/keyRings/ring-a",
                "projects/my-project/locations/global/keyRings/ring-b",
            )
            slot.captured.parent shouldBe "projects/my-project/locations/global"
        }
    }
}
```

Create `src/test/kotlin/com/kelvsyc/gradle/google/cloud/kms/ListCryptoKeysValueSourceSpec.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.CryptoKey
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.cloud.kms.v1.ListCryptoKeysRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListCryptoKeysValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns crypto key resource names from paginated response") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val keys = listOf(
                CryptoKey.newBuilder()
                    .setName("projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/key-1")
                    .build(),
                CryptoKey.newBuilder()
                    .setName("projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/key-2")
                    .build(),
            )
            val paged = mockk<KeyManagementServiceClient.ListCryptoKeysPagedResponse>()
            every { paged.iterateAll() } returns keys

            val slot = slot<ListCryptoKeysRequest>()
            every { client.listCryptoKeys(capture(slot)) } returns paged

            val provider = project.providers.ofKt(ListCryptoKeysValueSource::class) {
                parameters.service.set(service)
                parameters.projectId.set("my-project")
                parameters.location.set("global")
                parameters.keyRingId.set("my-ring")
            }

            provider.get() shouldBe listOf(
                "projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/key-1",
                "projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/key-2",
            )
            slot.captured.parent shouldBe "projects/my-project/locations/global/keyRings/my-ring"
        }
    }
}
```

Create `src/test/kotlin/com/kelvsyc/gradle/google/cloud/kms/ListCryptoKeyVersionsValueSourceSpec.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.CryptoKeyVersion
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.cloud.kms.v1.ListCryptoKeyVersionsRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListCryptoKeyVersionsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns crypto key version resource names from paginated response") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val versions = listOf(
                CryptoKeyVersion.newBuilder()
                    .setName("projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/my-key/cryptoKeyVersions/1")
                    .build(),
                CryptoKeyVersion.newBuilder()
                    .setName("projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/my-key/cryptoKeyVersions/2")
                    .build(),
            )
            val paged = mockk<KeyManagementServiceClient.ListCryptoKeyVersionsPagedResponse>()
            every { paged.iterateAll() } returns versions

            val slot = slot<ListCryptoKeyVersionsRequest>()
            every { client.listCryptoKeyVersions(capture(slot)) } returns paged

            val provider = project.providers.ofKt(ListCryptoKeyVersionsValueSource::class) {
                parameters.service.set(service)
                parameters.projectId.set("my-project")
                parameters.location.set("global")
                parameters.keyRingId.set("my-ring")
                parameters.cryptoKeyId.set("my-key")
            }

            provider.get() shouldBe listOf(
                "projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/my-key/cryptoKeyVersions/1",
                "projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/my-key/cryptoKeyVersions/2",
            )
            slot.captured.parent shouldBe
                "projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/my-key"
        }
    }
}
```

- [ ] **Step 2: Run to confirm compilation failure**

```bash
./gradlew :google-cloud-kms-base:test --tests "*.ListKeyRingsValueSourceSpec" --tests "*.ListCryptoKeysValueSourceSpec" --tests "*.ListCryptoKeyVersionsValueSourceSpec"
```

Expected: compilation failure — the three ValueSource classes do not exist yet.

- [ ] **Step 3: Create `ListKeyRingsValueSource`**

Create `src/main/kotlin/com/kelvsyc/gradle/google/cloud/kms/ListKeyRingsValueSource.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.ListKeyRingsRequest
import com.google.cloud.kms.v1.LocationName
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a list of key ring resource names within a GCP project
 * location.
 *
 * Pagination is handled internally via the high-level paged API.
 *
 * Each entry is the fully-qualified resource name in the form
 * `projects/{project}/locations/{location}/keyRings/{keyRing}`.
 */
abstract class ListKeyRingsValueSource : ValueSource<List<String>, ListKeyRingsValueSource.Parameters> {
    /**
     * Parameters for [ListKeyRingsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>

        /** GCP project ID. */
        val projectId: Property<String>

        /** GCP location (e.g. `"global"`, `"us-east1"`). */
        val location: Property<String>
    }

    override fun obtain(): List<String>? {
        val parent = LocationName.of(parameters.projectId.get(), parameters.location.get()).toString()
        val request = ListKeyRingsRequest.newBuilder().setParent(parent).build()
        return parameters.service.get().getClient().listKeyRings(request).iterateAll().map { it.name }
    }
}
```

- [ ] **Step 4: Create `ListCryptoKeysValueSource`**

Create `src/main/kotlin/com/kelvsyc/gradle/google/cloud/kms/ListCryptoKeysValueSource.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.KeyRingName
import com.google.cloud.kms.v1.ListCryptoKeysRequest
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a list of crypto key resource names within a key ring.
 *
 * Pagination is handled internally via the high-level paged API.
 *
 * Each entry is the fully-qualified resource name in the form
 * `projects/{project}/locations/{location}/keyRings/{keyRing}/cryptoKeys/{cryptoKey}`.
 */
abstract class ListCryptoKeysValueSource : ValueSource<List<String>, ListCryptoKeysValueSource.Parameters> {
    /**
     * Parameters for [ListCryptoKeysValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>

        /** GCP project ID. */
        val projectId: Property<String>

        /** GCP location (e.g. `"global"`, `"us-east1"`). */
        val location: Property<String>

        /** Key ring ID. */
        val keyRingId: Property<String>
    }

    override fun obtain(): List<String>? {
        val parent = KeyRingName.of(
            parameters.projectId.get(),
            parameters.location.get(),
            parameters.keyRingId.get()
        ).toString()
        val request = ListCryptoKeysRequest.newBuilder().setParent(parent).build()
        return parameters.service.get().getClient().listCryptoKeys(request).iterateAll().map { it.name }
    }
}
```

- [ ] **Step 5: Create `ListCryptoKeyVersionsValueSource`**

Create `src/main/kotlin/com/kelvsyc/gradle/google/cloud/kms/ListCryptoKeyVersionsValueSource.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.CryptoKeyName
import com.google.cloud.kms.v1.ListCryptoKeyVersionsRequest
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a list of crypto key version resource names within a
 * crypto key.
 *
 * Pagination is handled internally via the high-level paged API.
 *
 * Each entry is the fully-qualified resource name in the form
 * `projects/{project}/locations/{location}/keyRings/{keyRing}/cryptoKeys/{cryptoKey}/cryptoKeyVersions/{version}`.
 */
abstract class ListCryptoKeyVersionsValueSource :
    ValueSource<List<String>, ListCryptoKeyVersionsValueSource.Parameters> {
    /**
     * Parameters for [ListCryptoKeyVersionsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>

        /** GCP project ID. */
        val projectId: Property<String>

        /** GCP location (e.g. `"global"`, `"us-east1"`). */
        val location: Property<String>

        /** Key ring ID. */
        val keyRingId: Property<String>

        /** Crypto key ID. */
        val cryptoKeyId: Property<String>
    }

    override fun obtain(): List<String>? {
        val parent = CryptoKeyName.of(
            parameters.projectId.get(),
            parameters.location.get(),
            parameters.keyRingId.get(),
            parameters.cryptoKeyId.get()
        ).toString()
        val request = ListCryptoKeyVersionsRequest.newBuilder().setParent(parent).build()
        return parameters.service.get().getClient().listCryptoKeyVersions(request).iterateAll().map { it.name }
    }
}
```

- [ ] **Step 6: Run tests to verify all three pass**

```bash
./gradlew :google-cloud-kms-base:test --tests "*.ListKeyRingsValueSourceSpec" --tests "*.ListCryptoKeysValueSourceSpec" --tests "*.ListCryptoKeyVersionsValueSourceSpec"
```

Expected: `BUILD SUCCESSFUL`, 3 tests passed.

- [ ] **Step 7: Commit**

```bash
git add cores/google-cloud-kms-base/src/
git commit -m "feat(google-cloud-kms-base): add list ValueSources for KeyRings, CryptoKeys, and CryptoKeyVersions"
```

---

## Task 4: Get ValueSources

**Files:**
- Create: `src/main/kotlin/.../GetCryptoKeyValueSource.kt`
- Create: `src/main/kotlin/.../GetPublicKeyValueSource.kt`
- Create: `src/test/kotlin/.../GetCryptoKeyValueSourceSpec.kt`
- Create: `src/test/kotlin/.../GetPublicKeyValueSourceSpec.kt`

All paths under `cores/google-cloud-kms-base/`.

- [ ] **Step 1: Write failing tests for both get ValueSources**

Create `src/test/kotlin/com/kelvsyc/gradle/google/cloud/kms/GetCryptoKeyValueSourceSpec.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.api.gax.rpc.ApiException
import com.google.cloud.kms.v1.CryptoKey
import com.google.cloud.kms.v1.KeyManagementServiceClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetCryptoKeyValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns canonical crypto key name on success") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val keyName = "projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/my-key"
            every { client.getCryptoKey(keyName) } returns
                CryptoKey.newBuilder().setName(keyName).build()

            val provider = project.providers.ofKt(GetCryptoKeyValueSource::class) {
                parameters.service.set(service)
                parameters.cryptoKeyName.set(keyName)
            }

            provider.get() shouldBe keyName
        }

        test("obtain - returns null when ApiException is thrown") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            every { client.getCryptoKey(any<String>()) } throws mockk<ApiException>(relaxed = true)

            val provider = project.providers.ofKt(GetCryptoKeyValueSource::class) {
                parameters.service.set(service)
                parameters.cryptoKeyName.set("projects/my-project/locations/global/keyRings/r/cryptoKeys/missing")
            }

            provider.orNull.shouldBeNull()
        }
    }
}
```

Create `src/test/kotlin/com/kelvsyc/gradle/google/cloud/kms/GetPublicKeyValueSourceSpec.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.api.gax.rpc.ApiException
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.cloud.kms.v1.PublicKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetPublicKeyValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns PEM public key on success") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val versionName =
                "projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/my-key/cryptoKeyVersions/1"
            val pem = "-----BEGIN PUBLIC KEY-----\nMFkw...\n-----END PUBLIC KEY-----\n"
            every { client.getPublicKey(versionName) } returns
                PublicKey.newBuilder().setPem(pem).build()

            val provider = project.providers.ofKt(GetPublicKeyValueSource::class) {
                parameters.service.set(service)
                parameters.cryptoKeyVersionName.set(versionName)
            }

            provider.get() shouldBe pem
        }

        test("obtain - returns null when ApiException is thrown") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            every { client.getPublicKey(any<String>()) } throws mockk<ApiException>(relaxed = true)

            val provider = project.providers.ofKt(GetPublicKeyValueSource::class) {
                parameters.service.set(service)
                parameters.cryptoKeyVersionName.set(
                    "projects/my-project/locations/global/keyRings/r/cryptoKeys/k/cryptoKeyVersions/1"
                )
            }

            provider.orNull.shouldBeNull()
        }
    }
}
```

- [ ] **Step 2: Run to confirm compilation failure**

```bash
./gradlew :google-cloud-kms-base:test --tests "*.GetCryptoKeyValueSourceSpec" --tests "*.GetPublicKeyValueSourceSpec"
```

Expected: compilation failure.

- [ ] **Step 3: Create `GetCryptoKeyValueSource`**

Create `src/main/kotlin/com/kelvsyc/gradle/google/cloud/kms/GetCryptoKeyValueSource.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.api.gax.rpc.ApiException
import com.kelvsyc.gradle.logging.GradleLoggerDelegate
import com.kelvsyc.gradle.logging.warn
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation that retrieves the canonical name of a Cloud KMS crypto key.
 *
 * Returns `null` and logs a warning when the call throws [ApiException] (e.g. the key was not
 * found or the caller lacks `cloudkms.cryptoKeys.get` permission).
 */
abstract class GetCryptoKeyValueSource : ValueSource<String, GetCryptoKeyValueSource.Parameters> {
    companion object {
        val logger by GradleLoggerDelegate
    }

    /**
     * Parameters for [GetCryptoKeyValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>

        /**
         * Fully-qualified crypto key resource name, e.g.
         * `projects/{project}/locations/{location}/keyRings/{keyRing}/cryptoKeys/{cryptoKey}`.
         */
        val cryptoKeyName: Property<String>
    }

    override fun obtain(): String? {
        return try {
            parameters.service.get().getClient().getCryptoKey(parameters.cryptoKeyName.get()).name
        } catch (e: ApiException) {
            logger.warn(e) { "Unable to get Cloud KMS crypto key '${parameters.cryptoKeyName.get()}'" }
            null
        }
    }
}
```

- [ ] **Step 4: Create `GetPublicKeyValueSource`**

Create `src/main/kotlin/com/kelvsyc/gradle/google/cloud/kms/GetPublicKeyValueSource.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.api.gax.rpc.ApiException
import com.kelvsyc.gradle.logging.GradleLoggerDelegate
import com.kelvsyc.gradle.logging.warn
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation that retrieves the PEM-encoded public key for an asymmetric Cloud
 * KMS crypto key version.
 *
 * Returns `null` and logs a warning when the call throws [ApiException] (e.g. the version was not
 * found, is not an asymmetric key, or the caller lacks `cloudkms.cryptoKeyVersions.viewPublicKey`
 * permission).
 */
abstract class GetPublicKeyValueSource : ValueSource<String, GetPublicKeyValueSource.Parameters> {
    companion object {
        val logger by GradleLoggerDelegate
    }

    /**
     * Parameters for [GetPublicKeyValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>

        /**
         * Fully-qualified crypto key version resource name, e.g.
         * `projects/{project}/locations/{location}/keyRings/{keyRing}/cryptoKeys/{cryptoKey}/cryptoKeyVersions/{version}`.
         */
        val cryptoKeyVersionName: Property<String>
    }

    override fun obtain(): String? {
        return try {
            parameters.service.get().getClient().getPublicKey(parameters.cryptoKeyVersionName.get()).pem
        } catch (e: ApiException) {
            logger.warn(e) { "Unable to get public key for Cloud KMS version '${parameters.cryptoKeyVersionName.get()}'" }
            null
        }
    }
}
```

- [ ] **Step 5: Run tests to verify all pass**

```bash
./gradlew :google-cloud-kms-base:test --tests "*.GetCryptoKeyValueSourceSpec" --tests "*.GetPublicKeyValueSourceSpec"
```

Expected: `BUILD SUCCESSFUL`, 4 tests passed.

- [ ] **Step 6: Commit**

```bash
git add cores/google-cloud-kms-base/src/
git commit -m "feat(google-cloud-kms-base): add GetCryptoKeyValueSource and GetPublicKeyValueSource"
```

---

## Task 5: Symmetric WorkActions (`EncryptAction`, `DecryptAction`)

**Files:**
- Create: `src/main/kotlin/.../EncryptAction.kt`
- Create: `src/main/kotlin/.../DecryptAction.kt`
- Create: `src/test/kotlin/.../EncryptActionSpec.kt`
- Create: `src/test/kotlin/.../DecryptActionSpec.kt`

All paths under `cores/google-cloud-kms-base/`.

- [ ] **Step 1: Write failing tests**

Create `src/test/kotlin/com/kelvsyc/gradle/google/cloud/kms/EncryptActionSpec.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.EncryptRequest
import com.google.cloud.kms.v1.EncryptResponse
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.protobuf.ByteString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class EncryptActionSpec : FunSpec() {
    init {
        test("execute - encrypts plaintext file and writes ciphertext to output file") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val plaintext = "hello world".toByteArray()
            val ciphertext = "encrypted".toByteArray()
            val plaintextFile = File.createTempFile("plain", ".bin").also { it.writeBytes(plaintext) }
            val ciphertextFile = File.createTempFile("cipher", ".bin")

            val slot = slot<EncryptRequest>()
            every { client.encrypt(capture(slot)) } returns
                EncryptResponse.newBuilder()
                    .setCiphertextBlob(ByteString.copyFrom(ciphertext))
                    .build()

            val params = project.objects.newInstance<EncryptAction.Parameters>()
            params.service.set(service)
            params.cryptoKeyName.set("projects/p/locations/global/keyRings/r/cryptoKeys/k")
            params.plaintextFile.set(plaintextFile)
            params.ciphertextFile.set(ciphertextFile)

            val action = object : EncryptAction() {
                override fun getParameters() = params
            }
            action.execute()

            ciphertextFile.readBytes() shouldBe ciphertext
            slot.captured.name shouldBe "projects/p/locations/global/keyRings/r/cryptoKeys/k"
            slot.captured.plaintext shouldBe ByteString.copyFrom(plaintext)
        }
    }
}
```

Create `src/test/kotlin/com/kelvsyc/gradle/google/cloud/kms/DecryptActionSpec.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.DecryptRequest
import com.google.cloud.kms.v1.DecryptResponse
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.protobuf.ByteString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class DecryptActionSpec : FunSpec() {
    init {
        test("execute - decrypts ciphertext file and writes plaintext to output file") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val ciphertext = "encrypted".toByteArray()
            val plaintext = "hello world".toByteArray()
            val ciphertextFile = File.createTempFile("cipher", ".bin").also { it.writeBytes(ciphertext) }
            val plaintextFile = File.createTempFile("plain", ".bin")

            val slot = slot<DecryptRequest>()
            every { client.decrypt(capture(slot)) } returns
                DecryptResponse.newBuilder()
                    .setPlaintext(ByteString.copyFrom(plaintext))
                    .build()

            val params = project.objects.newInstance<DecryptAction.Parameters>()
            params.service.set(service)
            params.cryptoKeyName.set("projects/p/locations/global/keyRings/r/cryptoKeys/k")
            params.ciphertextFile.set(ciphertextFile)
            params.plaintextFile.set(plaintextFile)

            val action = object : DecryptAction() {
                override fun getParameters() = params
            }
            action.execute()

            plaintextFile.readBytes() shouldBe plaintext
            slot.captured.name shouldBe "projects/p/locations/global/keyRings/r/cryptoKeys/k"
            slot.captured.ciphertext shouldBe ByteString.copyFrom(ciphertext)
        }
    }
}
```

- [ ] **Step 2: Run to confirm compilation failure**

```bash
./gradlew :google-cloud-kms-base:test --tests "*.EncryptActionSpec" --tests "*.DecryptActionSpec"
```

Expected: compilation failure.

- [ ] **Step 3: Create `EncryptAction`**

Create `src/main/kotlin/com/kelvsyc/gradle/google/cloud/kms/EncryptAction.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.EncryptRequest
import com.google.protobuf.ByteString
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that encrypts the contents of [Parameters.plaintextFile] under a
 * Cloud KMS symmetric key and writes the resulting ciphertext blob to
 * [Parameters.ciphertextFile].
 *
 * The plaintext is read as raw bytes from disk; the ciphertext is written as the raw
 * `CiphertextBlob` returned by KMS, which embeds the key context required for decryption.
 */
abstract class EncryptAction : WorkAction<EncryptAction.Parameters> {
    /**
     * Parameters for [EncryptAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>

        /**
         * Fully-qualified crypto key resource name, e.g.
         * `projects/{project}/locations/{location}/keyRings/{keyRing}/cryptoKeys/{cryptoKey}`.
         */
        val cryptoKeyName: Property<String>

        /** Plaintext input file. */
        val plaintextFile: RegularFileProperty

        /** Ciphertext output file. */
        val ciphertextFile: RegularFileProperty
    }

    override fun execute() {
        val plaintext = ByteString.copyFrom(parameters.plaintextFile.get().asFile.readBytes())
        val request = EncryptRequest.newBuilder()
            .setName(parameters.cryptoKeyName.get())
            .setPlaintext(plaintext)
            .build()
        val response = parameters.service.get().getClient().encrypt(request)
        parameters.ciphertextFile.get().asFile.writeBytes(response.ciphertextBlob.toByteArray())
    }
}
```

- [ ] **Step 4: Create `DecryptAction`**

Create `src/main/kotlin/com/kelvsyc/gradle/google/cloud/kms/DecryptAction.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.DecryptRequest
import com.google.protobuf.ByteString
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that decrypts the ciphertext blob in [Parameters.ciphertextFile]
 * using a Cloud KMS symmetric key and writes the resulting plaintext bytes to
 * [Parameters.plaintextFile].
 */
abstract class DecryptAction : WorkAction<DecryptAction.Parameters> {
    /**
     * Parameters for [DecryptAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>

        /**
         * Fully-qualified crypto key resource name, e.g.
         * `projects/{project}/locations/{location}/keyRings/{keyRing}/cryptoKeys/{cryptoKey}`.
         */
        val cryptoKeyName: Property<String>

        /** Ciphertext input file. */
        val ciphertextFile: RegularFileProperty

        /** Plaintext output file. */
        val plaintextFile: RegularFileProperty
    }

    override fun execute() {
        val ciphertext = ByteString.copyFrom(parameters.ciphertextFile.get().asFile.readBytes())
        val request = DecryptRequest.newBuilder()
            .setName(parameters.cryptoKeyName.get())
            .setCiphertext(ciphertext)
            .build()
        val response = parameters.service.get().getClient().decrypt(request)
        parameters.plaintextFile.get().asFile.writeBytes(response.plaintext.toByteArray())
    }
}
```

- [ ] **Step 5: Run tests to verify both pass**

```bash
./gradlew :google-cloud-kms-base:test --tests "*.EncryptActionSpec" --tests "*.DecryptActionSpec"
```

Expected: `BUILD SUCCESSFUL`, 2 tests passed.

- [ ] **Step 6: Commit**

```bash
git add cores/google-cloud-kms-base/src/
git commit -m "feat(google-cloud-kms-base): add symmetric EncryptAction and DecryptAction"
```

---

## Task 6: Asymmetric WorkActions (`AsymmetricSignAction`, `AsymmetricDecryptAction`)

**Files:**
- Create: `src/main/kotlin/.../AsymmetricSignAction.kt`
- Create: `src/main/kotlin/.../AsymmetricDecryptAction.kt`
- Create: `src/test/kotlin/.../AsymmetricSignActionSpec.kt`
- Create: `src/test/kotlin/.../AsymmetricDecryptActionSpec.kt`

All paths under `cores/google-cloud-kms-base/`.

- [ ] **Step 1: Write failing tests**

Create `src/test/kotlin/com/kelvsyc/gradle/google/cloud/kms/AsymmetricSignActionSpec.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.AsymmetricSignRequest
import com.google.cloud.kms.v1.AsymmetricSignResponse
import com.google.cloud.kms.v1.Digest
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.protobuf.ByteString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import java.security.MessageDigest

class AsymmetricSignActionSpec : FunSpec() {
    init {
        test("execute - signs SHA-256 digest of data file and writes signature to output file") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val data = "artifact content".toByteArray()
            val signature = "sig-bytes".toByteArray()
            val dataFile = File.createTempFile("data", ".bin").also { it.writeBytes(data) }
            val signatureFile = File.createTempFile("sig", ".bin")

            val slot = slot<AsymmetricSignRequest>()
            every { client.asymmetricSign(capture(slot)) } returns
                AsymmetricSignResponse.newBuilder()
                    .setSignature(ByteString.copyFrom(signature))
                    .build()

            val params = project.objects.newInstance<AsymmetricSignAction.Parameters>()
            params.service.set(service)
            params.cryptoKeyVersionName.set(
                "projects/p/locations/global/keyRings/r/cryptoKeys/k/cryptoKeyVersions/1"
            )
            params.digestAlgorithm.set("SHA256")
            params.dataFile.set(dataFile)
            params.signatureFile.set(signatureFile)

            val action = object : AsymmetricSignAction() {
                override fun getParameters() = params
            }
            action.execute()

            signatureFile.readBytes() shouldBe signature
            slot.captured.name shouldBe
                "projects/p/locations/global/keyRings/r/cryptoKeys/k/cryptoKeyVersions/1"
            val expectedDigest = MessageDigest.getInstance("SHA-256").digest(data)
            slot.captured.digest.sha256 shouldBe ByteString.copyFrom(expectedDigest)
        }
    }
}
```

Create `src/test/kotlin/com/kelvsyc/gradle/google/cloud/kms/AsymmetricDecryptActionSpec.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.AsymmetricDecryptRequest
import com.google.cloud.kms.v1.AsymmetricDecryptResponse
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.protobuf.ByteString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class AsymmetricDecryptActionSpec : FunSpec() {
    init {
        test("execute - decrypts ciphertext file and writes plaintext to output file") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val ciphertext = "rsa-encrypted".toByteArray()
            val plaintext = "secret data".toByteArray()
            val ciphertextFile = File.createTempFile("cipher", ".bin").also { it.writeBytes(ciphertext) }
            val plaintextFile = File.createTempFile("plain", ".bin")

            val slot = slot<AsymmetricDecryptRequest>()
            every { client.asymmetricDecrypt(capture(slot)) } returns
                AsymmetricDecryptResponse.newBuilder()
                    .setPlaintext(ByteString.copyFrom(plaintext))
                    .build()

            val params = project.objects.newInstance<AsymmetricDecryptAction.Parameters>()
            params.service.set(service)
            params.cryptoKeyVersionName.set(
                "projects/p/locations/global/keyRings/r/cryptoKeys/k/cryptoKeyVersions/1"
            )
            params.ciphertextFile.set(ciphertextFile)
            params.plaintextFile.set(plaintextFile)

            val action = object : AsymmetricDecryptAction() {
                override fun getParameters() = params
            }
            action.execute()

            plaintextFile.readBytes() shouldBe plaintext
            slot.captured.name shouldBe
                "projects/p/locations/global/keyRings/r/cryptoKeys/k/cryptoKeyVersions/1"
            slot.captured.ciphertext shouldBe ByteString.copyFrom(ciphertext)
        }
    }
}
```

- [ ] **Step 2: Run to confirm compilation failure**

```bash
./gradlew :google-cloud-kms-base:test --tests "*.AsymmetricSignActionSpec" --tests "*.AsymmetricDecryptActionSpec"
```

Expected: compilation failure.

- [ ] **Step 3: Create `AsymmetricSignAction`**

Create `src/main/kotlin/com/kelvsyc/gradle/google/cloud/kms/AsymmetricSignAction.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.AsymmetricSignRequest
import com.google.cloud.kms.v1.Digest
import com.google.protobuf.ByteString
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import java.security.MessageDigest

/**
 * [WorkAction] implementation that signs the contents of [Parameters.dataFile] using an asymmetric
 * Cloud KMS key version and writes the resulting signature to [Parameters.signatureFile].
 *
 * The data is hashed locally using [Parameters.digestAlgorithm] before being sent to KMS as a
 * [Digest]. This is necessary for payloads that may exceed the 64 KB raw-data limit of the KMS
 * API (e.g. JAR files, release artifacts).
 *
 * [Parameters.digestAlgorithm] must be one of `"SHA256"`, `"SHA384"`, or `"SHA512"`, matching the
 * algorithm of the key version.
 */
abstract class AsymmetricSignAction : WorkAction<AsymmetricSignAction.Parameters> {
    /**
     * Parameters for [AsymmetricSignAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>

        /**
         * Fully-qualified crypto key version resource name, e.g.
         * `projects/{project}/locations/{location}/keyRings/{keyRing}/cryptoKeys/{cryptoKey}/cryptoKeyVersions/{version}`.
         */
        val cryptoKeyVersionName: Property<String>

        /**
         * Hash algorithm to apply locally before sending to KMS. Must match the key version's
         * algorithm. Accepted values: `"SHA256"`, `"SHA384"`, `"SHA512"`.
         */
        val digestAlgorithm: Property<String>

        /** Data input file to sign. */
        val dataFile: RegularFileProperty

        /** Signature output file. */
        val signatureFile: RegularFileProperty
    }

    override fun execute() {
        val data = parameters.dataFile.get().asFile.readBytes()
        val digestBytes = MessageDigest.getInstance("SHA-${parameters.digestAlgorithm.get().removePrefix("SHA")}")
            .digest(data)
        val digest = when (parameters.digestAlgorithm.get()) {
            "SHA256" -> Digest.newBuilder().setSha256(ByteString.copyFrom(digestBytes)).build()
            "SHA384" -> Digest.newBuilder().setSha384(ByteString.copyFrom(digestBytes)).build()
            "SHA512" -> Digest.newBuilder().setSha512(ByteString.copyFrom(digestBytes)).build()
            else -> error("Unsupported digest algorithm: ${parameters.digestAlgorithm.get()}. Use SHA256, SHA384, or SHA512.")
        }
        val request = AsymmetricSignRequest.newBuilder()
            .setName(parameters.cryptoKeyVersionName.get())
            .setDigest(digest)
            .build()
        val response = parameters.service.get().getClient().asymmetricSign(request)
        parameters.signatureFile.get().asFile.writeBytes(response.signature.toByteArray())
    }
}
```

- [ ] **Step 4: Create `AsymmetricDecryptAction`**

Create `src/main/kotlin/com/kelvsyc/gradle/google/cloud/kms/AsymmetricDecryptAction.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.AsymmetricDecryptRequest
import com.google.protobuf.ByteString
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that decrypts the ciphertext blob in [Parameters.ciphertextFile]
 * using an asymmetric Cloud KMS key version and writes the resulting plaintext bytes to
 * [Parameters.plaintextFile].
 */
abstract class AsymmetricDecryptAction : WorkAction<AsymmetricDecryptAction.Parameters> {
    /**
     * Parameters for [AsymmetricDecryptAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>

        /**
         * Fully-qualified crypto key version resource name, e.g.
         * `projects/{project}/locations/{location}/keyRings/{keyRing}/cryptoKeys/{cryptoKey}/cryptoKeyVersions/{version}`.
         */
        val cryptoKeyVersionName: Property<String>

        /** Ciphertext input file. */
        val ciphertextFile: RegularFileProperty

        /** Plaintext output file. */
        val plaintextFile: RegularFileProperty
    }

    override fun execute() {
        val ciphertext = ByteString.copyFrom(parameters.ciphertextFile.get().asFile.readBytes())
        val request = AsymmetricDecryptRequest.newBuilder()
            .setName(parameters.cryptoKeyVersionName.get())
            .setCiphertext(ciphertext)
            .build()
        val response = parameters.service.get().getClient().asymmetricDecrypt(request)
        parameters.plaintextFile.get().asFile.writeBytes(response.plaintext.toByteArray())
    }
}
```

- [ ] **Step 5: Run tests to verify both pass**

```bash
./gradlew :google-cloud-kms-base:test --tests "*.AsymmetricSignActionSpec" --tests "*.AsymmetricDecryptActionSpec"
```

Expected: `BUILD SUCCESSFUL`, 2 tests passed.

- [ ] **Step 6: Commit**

```bash
git add cores/google-cloud-kms-base/src/
git commit -m "feat(google-cloud-kms-base): add AsymmetricSignAction and AsymmetricDecryptAction"
```

---

## Task 7: `MacSignAction`

**Files:**
- Create: `src/main/kotlin/.../MacSignAction.kt`
- Create: `src/test/kotlin/.../MacSignActionSpec.kt`

All paths under `cores/google-cloud-kms-base/`.

- [ ] **Step 1: Write failing test**

Create `src/test/kotlin/com/kelvsyc/gradle/google/cloud/kms/MacSignActionSpec.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.cloud.kms.v1.MacSignRequest
import com.google.cloud.kms.v1.MacSignResponse
import com.google.protobuf.ByteString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class MacSignActionSpec : FunSpec() {
    init {
        test("execute - computes MAC over data file and writes mac to output file") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val data = "payload data".toByteArray()
            val mac = "hmac-bytes".toByteArray()
            val dataFile = File.createTempFile("data", ".bin").also { it.writeBytes(data) }
            val macFile = File.createTempFile("mac", ".bin")

            val slot = slot<MacSignRequest>()
            every { client.macSign(capture(slot)) } returns
                MacSignResponse.newBuilder()
                    .setMac(ByteString.copyFrom(mac))
                    .build()

            val params = project.objects.newInstance<MacSignAction.Parameters>()
            params.service.set(service)
            params.cryptoKeyVersionName.set(
                "projects/p/locations/global/keyRings/r/cryptoKeys/k/cryptoKeyVersions/1"
            )
            params.dataFile.set(dataFile)
            params.macFile.set(macFile)

            val action = object : MacSignAction() {
                override fun getParameters() = params
            }
            action.execute()

            macFile.readBytes() shouldBe mac
            slot.captured.name shouldBe
                "projects/p/locations/global/keyRings/r/cryptoKeys/k/cryptoKeyVersions/1"
            slot.captured.data shouldBe ByteString.copyFrom(data)
        }
    }
}
```

- [ ] **Step 2: Run to confirm compilation failure**

```bash
./gradlew :google-cloud-kms-base:test --tests "*.MacSignActionSpec"
```

Expected: compilation failure.

- [ ] **Step 3: Create `MacSignAction`**

Create `src/main/kotlin/com/kelvsyc/gradle/google/cloud/kms/MacSignAction.kt`:

```kotlin
package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.MacSignRequest
import com.google.protobuf.ByteString
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that computes an HMAC over the contents of [Parameters.dataFile]
 * using a Cloud KMS MAC key version and writes the resulting MAC to [Parameters.macFile].
 */
abstract class MacSignAction : WorkAction<MacSignAction.Parameters> {
    /**
     * Parameters for [MacSignAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>

        /**
         * Fully-qualified crypto key version resource name, e.g.
         * `projects/{project}/locations/{location}/keyRings/{keyRing}/cryptoKeys/{cryptoKey}/cryptoKeyVersions/{version}`.
         */
        val cryptoKeyVersionName: Property<String>

        /** Data input file to authenticate. */
        val dataFile: RegularFileProperty

        /** MAC output file. */
        val macFile: RegularFileProperty
    }

    override fun execute() {
        val data = ByteString.copyFrom(parameters.dataFile.get().asFile.readBytes())
        val request = MacSignRequest.newBuilder()
            .setName(parameters.cryptoKeyVersionName.get())
            .setData(data)
            .build()
        val response = parameters.service.get().getClient().macSign(request)
        parameters.macFile.get().asFile.writeBytes(response.mac.toByteArray())
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :google-cloud-kms-base:test --tests "*.MacSignActionSpec"
```

Expected: `BUILD SUCCESSFUL`, 1 test passed.

- [ ] **Step 5: Commit**

```bash
git add cores/google-cloud-kms-base/src/
git commit -m "feat(google-cloud-kms-base): add MacSignAction"
```

---

## Task 8: Full test suite, detekt, README

**Files:**
- Modify: `README.md` (root)
- Create: `cores/google-cloud-kms-base/README.md`

- [ ] **Step 1: Run the full component test suite and detekt**

```bash
./gradlew :google-cloud-kms-base:test :google-cloud-kms-base:detekt
```

Expected: `BUILD SUCCESSFUL`, all tests pass, no detekt violations.

Fix any detekt violations before proceeding. Common issues:
- `WildcardImport`: replace `import com.google.cloud.kms.v1.*` with explicit imports
- `MagicNumber`: none expected (no numeric literals in production code)
- Missing trailing newline on `.kt` files (added automatically by the PostToolUse hook)

- [ ] **Step 2: Create `cores/google-cloud-kms-base/README.md`**

```markdown
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
```

- [ ] **Step 3: Add `google-cloud-kms-base` to the root README**

In `README.md`, find the "Other bases" table (lines containing `google-cloud-artifact-registry-base`). Add a new row after `google-cloud-pubsub-base`:

```markdown
| `google-cloud-kms-base` | GCP Cloud KMS | library |
```

- [ ] **Step 4: Run the full build to confirm everything is clean**

```bash
./gradlew :google-cloud-kms-base:build
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add cores/google-cloud-kms-base/README.md README.md
git commit -m "docs(google-cloud-kms-base): add component README and update root README"
```
