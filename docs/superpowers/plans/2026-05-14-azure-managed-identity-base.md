# Azure Managed Identity Base — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a new `azure-managed-identity-base` component that exposes raw Azure Managed Identity OAuth2 tokens via `AccessTokenValueSource` and IMDS compute/identity metadata via three IMDS-backed `ValueSource`s.

**Architecture:** Two `AbstractClientBuildService` subclasses — `ManagedIdentityCredentialBuildService` (wraps Azure Identity SDK) and `AzureImdsClientBuildService` (Retrofit/OkHttp client for the IMDS endpoint at `http://169.254.169.254/metadata/`) — each wired to their respective `ValueSource` implementations. The component intentionally does NOT extend `AbstractAzureClientBuildService`; it is a credential *source*, not a consumer.

**Tech Stack:** Kotlin, Gradle (kotlin-gradle-library convention), Azure Identity SDK (`azure-identity`), Retrofit 2, OkHttp 5, Moshi 1.15, MockK (tests), Kotest (tests)

---

## File map

```
cores/azure-managed-identity-base/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
└── src/
    ├── main/kotlin/com/kelvsyc/gradle/azure/identity/
    │   ├── AzureImdsService.kt
    │   ├── model/
    │   │   ├── AzureComputeMetadata.kt
    │   │   ├── AzureAttestedData.kt
    │   │   └── AzureManagedIdentityInfo.kt
    │   ├── AzureImdsClientBuildService.kt
    │   ├── ManagedIdentityCredentialBuildService.kt
    │   ├── ManagedIdentityCredentialBuildServiceParamsExtensions.kt
    │   ├── AccessTokenValueSource.kt
    │   ├── AzureComputeMetadataValueSource.kt
    │   ├── GetManagedIdentityInfoValueSource.kt
    │   └── AzureAttestedDataValueSource.kt
    └── test/kotlin/com/kelvsyc/gradle/azure/identity/
        ├── ProviderFactoryExtensions.kt
        ├── MockAzureImdsClientBuildService.kt
        ├── MockManagedIdentityCredentialBuildService.kt
        ├── ManagedIdentityCredentialBuildServiceSpec.kt
        ├── AccessTokenValueSourceSpec.kt
        ├── AzureImdsClientBuildServiceSpec.kt
        ├── AzureComputeMetadataValueSourceSpec.kt
        ├── GetManagedIdentityInfoValueSourceSpec.kt
        └── AzureAttestedDataValueSourceSpec.kt
```

---

### Task 1: Scaffold the component

**Files:**
- Create: `cores/azure-managed-identity-base/settings.gradle.kts`
- Create: `cores/azure-managed-identity-base/build.gradle.kts`
- Create: `cores/azure-managed-identity-base/gradle.properties`

- [ ] **Step 1.1: Create `settings.gradle.kts`**

```kotlin
// cores/azure-managed-identity-base/settings.gradle.kts
pluginManagement {
    includeBuild("../../gradle/settings")
}

plugins {
    id("com.kelvsyc.internal")
}

includeBuild("../clients-base")
// No azure-extensions — this component is a credential source, not consumer
```

- [ ] **Step 1.2: Create `build.gradle.kts`**

```kotlin
// cores/azure-managed-identity-base/build.gradle.kts
import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-gradle-library")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Azure Managed Identity Base")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")
    api(libs.azure.identity)
    api(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.retrofit.converter.moshi)
    api(libs.moshi)
    implementation(libs.moshi.kotlin)
    testImplementation(libs.mockk)
}

tasks.test {
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}
```

- [ ] **Step 1.3: Create `gradle.properties`**

```properties
# cores/azure-managed-identity-base/gradle.properties
# Dokka V2 migration
org.jetbrains.dokka.experimental.gradle.pluginMode=V2EnabledWithHelpers
```

- [ ] **Step 1.4: Create source directory structure**

```bash
mkdir -p cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/model
mkdir -p cores/azure-managed-identity-base/src/test/kotlin/com/kelvsyc/gradle/azure/identity
```

- [ ] **Step 1.5: Verify the component is picked up by the root build**

```bash
./gradlew :azure-managed-identity-base:tasks --dry-run 2>&1 | head -20
```

Expected: no errors, task list shows Gradle build tasks.

- [ ] **Step 1.6: Commit**

```bash
git add cores/azure-managed-identity-base/
git commit -m "chore: scaffold azure-managed-identity-base component"
```

---

### Task 2: Moshi model classes and Retrofit interface

**Files:**
- Create: `cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/model/AzureComputeMetadata.kt`
- Create: `cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/model/AzureAttestedData.kt`
- Create: `cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/model/AzureManagedIdentityInfo.kt`
- Create: `cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/AzureImdsService.kt`

- [ ] **Step 2.1: Write `AzureComputeMetadata.kt`**

```kotlin
// cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/model/AzureComputeMetadata.kt
package com.kelvsyc.gradle.azure.identity.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Compute metadata returned by the Azure Instance Metadata Service `/instance/compute` endpoint.
 */
@JsonClass(generateAdapter = false)
data class AzureComputeMetadata(
    /** Azure subscription ID in which the VM is deployed. */
    @Json(name = "subscriptionId") val subscriptionId: String? = null,
    /** Resource group containing the VM. */
    @Json(name = "resourceGroupName") val resourceGroupName: String? = null,
    /** Name of the VM. */
    @Json(name = "name") val name: String? = null,
    /** Azure region where the VM is located. */
    @Json(name = "location") val location: String? = null,
    /** Unique identifier for the VM. */
    @Json(name = "vmId") val vmId: String? = null,
    /** VM size (SKU). */
    @Json(name = "vmSize") val vmSize: String? = null,
    /** OS type (`Linux` or `Windows`). */
    @Json(name = "osType") val osType: String? = null,
)
```

- [ ] **Step 2.2: Write `AzureAttestedData.kt`**

```kotlin
// cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/model/AzureAttestedData.kt
package com.kelvsyc.gradle.azure.identity.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Attested data returned by the Azure Instance Metadata Service `/attested/document` endpoint.
 */
@JsonClass(generateAdapter = false)
data class AzureAttestedData(
    /** Encoding of the signature (e.g. `pkcs7`). */
    @Json(name = "encoding") val encoding: String,
    /** Base64-encoded signed document. */
    @Json(name = "signature") val signature: String,
)
```

- [ ] **Step 2.3: Write `AzureManagedIdentityInfo.kt`**

```kotlin
// cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/model/AzureManagedIdentityInfo.kt
package com.kelvsyc.gradle.azure.identity.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Managed identity information returned by the Azure Instance Metadata Service `/identity/info` endpoint.
 */
@JsonClass(generateAdapter = false)
data class AzureManagedIdentityInfo(
    /** Client ID of the system-assigned managed identity. */
    @Json(name = "clientId") val clientId: String,
    /** Object ID of the system-assigned managed identity. */
    @Json(name = "objectId") val objectId: String,
)
```

- [ ] **Step 2.4: Write `AzureImdsService.kt`**

```kotlin
// cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/AzureImdsService.kt
package com.kelvsyc.gradle.azure.identity

import com.kelvsyc.gradle.azure.identity.model.AzureAttestedData
import com.kelvsyc.gradle.azure.identity.model.AzureComputeMetadata
import com.kelvsyc.gradle.azure.identity.model.AzureManagedIdentityInfo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the [Azure Instance Metadata Service](https://learn.microsoft.com/en-us/azure/virtual-machines/instance-metadata-service).
 *
 * All calls require the `Metadata: true` header, which is added globally by
 * [AzureImdsClientBuildService].
 */
interface AzureImdsService {
    /** Retrieves compute metadata for the current VM instance. */
    @GET("instance/compute")
    fun getComputeMetadata(@Query("api-version") apiVersion: String): Call<AzureComputeMetadata>

    /** Retrieves the attested document for the current VM instance. */
    @GET("attested/document")
    fun getAttestedData(
        @Query("api-version") apiVersion: String,
        @Query("nonce") nonce: String? = null,
    ): Call<AzureAttestedData>

    /** Retrieves managed identity information for the current VM instance. */
    @GET("identity/info")
    fun getManagedIdentityInfo(@Query("api-version") apiVersion: String): Call<AzureManagedIdentityInfo>
}
```

- [ ] **Step 2.5: Compile check**

```bash
./gradlew :azure-managed-identity-base:compileKotlin
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2.6: Commit**

```bash
git add cores/azure-managed-identity-base/src/
git commit -m "feat(azure-managed-identity-base): add IMDS Retrofit interface and Moshi model classes"
```

---

### Task 3: `AzureImdsClientBuildService`

**Files:**
- Create: `cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/AzureImdsClientBuildService.kt`

- [ ] **Step 3.1: Write the failing test**

```kotlin
// cores/azure-managed-identity-base/src/test/kotlin/com/kelvsyc/gradle/azure/identity/AzureImdsClientBuildServiceSpec.kt
package com.kelvsyc.gradle.azure.identity

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class AzureImdsClientBuildServiceSpec : FunSpec() {
    init {
        test("getClient - returns non-null AzureImdsService") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices
                .registerIfAbsent("imds", AzureImdsClientBuildService::class) {}

            service.get().getClient().shouldNotBeNull()
        }
    }
}
```

- [ ] **Step 3.2: Run failing test**

```bash
./gradlew :azure-managed-identity-base:test --tests "com.kelvsyc.gradle.azure.identity.AzureImdsClientBuildServiceSpec" 2>&1 | tail -20
```

Expected: FAIL — class not found.

- [ ] **Step 3.3: Implement `AzureImdsClientBuildService`**

```kotlin
// cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/AzureImdsClientBuildService.kt
package com.kelvsyc.gradle.azure.identity

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.gradle.api.services.BuildServiceParameters
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Build service managing an [AzureImdsService] Retrofit client.
 *
 * The client targets the Azure Instance Metadata Service (IMDS) at `http://169.254.169.254/metadata/`
 * and automatically adds the `Metadata: true` header required by all IMDS endpoints.
 *
 * No user-configurable parameters are needed — the IMDS endpoint is fixed per the Azure specification.
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent]:
 *
 * ```kotlin
 * val imds = gradle.sharedServices.registerIfAbsent("imds", AzureImdsClientBuildService::class) {}
 * ```
 */
abstract class AzureImdsClientBuildService :
    AbstractClientBuildService<AzureImdsService, BuildServiceParameters.None>() {

    override fun createClient(): AzureImdsService {
        val metadataInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Metadata", "true")
                .build()
            chain.proceed(request)
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(metadataInterceptor)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(AzureImdsService::class.java)
    }

    private companion object {
        private const val BASE_URL = "http://169.254.169.254/metadata/"
    }
}
```

- [ ] **Step 3.4: Run passing test**

```bash
./gradlew :azure-managed-identity-base:test --tests "com.kelvsyc.gradle.azure.identity.AzureImdsClientBuildServiceSpec"
```

Expected: BUILD SUCCESSFUL, 1 test passed.

- [ ] **Step 3.5: Commit**

```bash
git add cores/azure-managed-identity-base/src/
git commit -m "feat(azure-managed-identity-base): add AzureImdsClientBuildService"
```

---

### Task 4: `ManagedIdentityCredentialBuildService` + params extensions

**Files:**
- Create: `cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/ManagedIdentityCredentialBuildService.kt`
- Create: `cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/ManagedIdentityCredentialBuildServiceParamsExtensions.kt`

- [ ] **Step 4.1: Write the failing test**

```kotlin
// cores/azure-managed-identity-base/src/test/kotlin/com/kelvsyc/gradle/azure/identity/ManagedIdentityCredentialBuildServiceSpec.kt
package com.kelvsyc.gradle.azure.identity

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ManagedIdentityCredentialBuildServiceSpec : FunSpec() {
    init {
        test("getClient - system-assigned returns non-null credential") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices
                .registerIfAbsent("mi", ManagedIdentityCredentialBuildService::class) {
                    parameters.systemAssigned()
                }

            service.get().getClient().shouldNotBeNull()
        }

        test("getClient - user-assigned by clientId returns non-null credential") {
            val project = ProjectBuilder.builder().build()
            val service = project.gradle.sharedServices
                .registerIfAbsent("mi-client", ManagedIdentityCredentialBuildService::class) {
                    parameters.userAssigned("00000000-0000-0000-0000-000000000000")
                }

            service.get().getClient().shouldNotBeNull()
        }
    }
}
```

- [ ] **Step 4.2: Run failing test**

```bash
./gradlew :azure-managed-identity-base:test --tests "com.kelvsyc.gradle.azure.identity.ManagedIdentityCredentialBuildServiceSpec" 2>&1 | tail -20
```

Expected: FAIL — class not found.

- [ ] **Step 4.3: Implement `ManagedIdentityCredentialBuildService`**

```kotlin
// cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/ManagedIdentityCredentialBuildService.kt
package com.kelvsyc.gradle.azure.identity

import com.azure.identity.ManagedIdentityCredential
import com.azure.identity.ManagedIdentityCredentialBuilder
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing a [ManagedIdentityCredential] for a system-assigned or user-assigned managed identity.
 *
 * Register using [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent] and configure the identity
 * selector via the extension functions on [Params]. Leave all params unset for system-assigned identity:
 *
 * ```kotlin
 * val mi = gradle.sharedServices.registerIfAbsent("mi", ManagedIdentityCredentialBuildService::class) {
 *     parameters.systemAssigned()       // explicit no-op; documents intent
 *     // parameters.userAssigned("00000000-0000-0000-0000-000000000000")
 *     // parameters.userAssignedByObjectId("...")
 *     // parameters.userAssignedByResourceId("/subscriptions/.../resourceGroups/.../providers/...")
 * }
 * ```
 */
abstract class ManagedIdentityCredentialBuildService :
    AbstractClientBuildService<ManagedIdentityCredential, ManagedIdentityCredentialBuildService.Params>() {

    /**
     * Configuration parameters for [ManagedIdentityCredentialBuildService].
     *
     * All parameters are optional. When none are set, the build service creates a system-assigned credential.
     * Use only one of [clientId], [objectId], or [resourceId] to select a user-assigned identity.
     */
    interface Params : BuildServiceParameters {
        /** Client/application ID for a user-assigned managed identity. */
        val clientId: Property<String>
        /** Object ID for a user-assigned managed identity. */
        val objectId: Property<String>
        /** ARM resource ID for a user-assigned managed identity. */
        val resourceId: Property<String>
    }

    override fun createClient(): ManagedIdentityCredential {
        val builder = ManagedIdentityCredentialBuilder()
        parameters.clientId.orNull?.let(builder::clientId)
        parameters.objectId.orNull?.let(builder::objectId)
        parameters.resourceId.orNull?.let(builder::resourceId)
        return builder.build()
    }
}
```

- [ ] **Step 4.4: Implement `ManagedIdentityCredentialBuildServiceParamsExtensions.kt`**

```kotlin
// cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/ManagedIdentityCredentialBuildServiceParamsExtensions.kt
package com.kelvsyc.gradle.azure.identity

import org.gradle.api.provider.Provider

/**
 * Selects the system-assigned managed identity. This is a no-op (all params remain unset) and exists
 * solely to document intent at the call site.
 */
fun ManagedIdentityCredentialBuildService.Params.systemAssigned() {
    // intentionally empty — system-assigned is the default when no params are set
}

/** Selects a user-assigned managed identity by client/application ID. */
fun ManagedIdentityCredentialBuildService.Params.userAssigned(clientId: String) {
    this.clientId.set(clientId)
}

/** Selects a user-assigned managed identity by client/application ID from a [Provider]. */
fun ManagedIdentityCredentialBuildService.Params.userAssigned(clientId: Provider<String>) {
    this.clientId.set(clientId)
}

/** Selects a user-assigned managed identity by object ID. */
fun ManagedIdentityCredentialBuildService.Params.userAssignedByObjectId(objectId: String) {
    this.objectId.set(objectId)
}

/** Selects a user-assigned managed identity by object ID from a [Provider]. */
fun ManagedIdentityCredentialBuildService.Params.userAssignedByObjectId(objectId: Provider<String>) {
    this.objectId.set(objectId)
}

/** Selects a user-assigned managed identity by ARM resource ID. */
fun ManagedIdentityCredentialBuildService.Params.userAssignedByResourceId(resourceId: String) {
    this.resourceId.set(resourceId)
}

/** Selects a user-assigned managed identity by ARM resource ID from a [Provider]. */
fun ManagedIdentityCredentialBuildService.Params.userAssignedByResourceId(resourceId: Provider<String>) {
    this.resourceId.set(resourceId)
}
```

- [ ] **Step 4.5: Run passing tests**

```bash
./gradlew :azure-managed-identity-base:test --tests "com.kelvsyc.gradle.azure.identity.ManagedIdentityCredentialBuildServiceSpec"
```

Expected: BUILD SUCCESSFUL, 2 tests passed.

- [ ] **Step 4.6: Commit**

```bash
git add cores/azure-managed-identity-base/src/
git commit -m "feat(azure-managed-identity-base): add ManagedIdentityCredentialBuildService and params extensions"
```

---

### Task 5: Mock build services and `ProviderFactoryExtensions`

These test helpers are needed by ValueSource tests in Tasks 6 and 7.

**Files:**
- Create: `cores/azure-managed-identity-base/src/test/kotlin/com/kelvsyc/gradle/azure/identity/ProviderFactoryExtensions.kt`
- Create: `cores/azure-managed-identity-base/src/test/kotlin/com/kelvsyc/gradle/azure/identity/MockAzureImdsClientBuildService.kt`
- Create: `cores/azure-managed-identity-base/src/test/kotlin/com/kelvsyc/gradle/azure/identity/MockManagedIdentityCredentialBuildService.kt`

- [ ] **Step 5.1: Write `ProviderFactoryExtensions.kt`**

```kotlin
// cores/azure-managed-identity-base/src/test/kotlin/com/kelvsyc/gradle/azure/identity/ProviderFactoryExtensions.kt
package com.kelvsyc.gradle.azure.identity

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
    configuration: ValueSourceSpec<P>.() -> Unit,
) = of(valueSourceType, configuration)
```

- [ ] **Step 5.2: Write `MockAzureImdsClientBuildService.kt`**

```kotlin
// cores/azure-managed-identity-base/src/test/kotlin/com/kelvsyc/gradle/azure/identity/MockAzureImdsClientBuildService.kt
package com.kelvsyc.gradle.azure.identity

/**
 * Test-only [AzureImdsClientBuildService] that returns a pre-supplied mock [AzureImdsService].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockAzureImdsClientBuildService : AzureImdsClientBuildService() {
    override fun createClient(): AzureImdsService =
        checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: AzureImdsService? = null
    }
}
```

- [ ] **Step 5.3: Write `MockManagedIdentityCredentialBuildService.kt`**

```kotlin
// cores/azure-managed-identity-base/src/test/kotlin/com/kelvsyc/gradle/azure/identity/MockManagedIdentityCredentialBuildService.kt
package com.kelvsyc.gradle.azure.identity

import com.azure.identity.ManagedIdentityCredential

/**
 * Test-only [ManagedIdentityCredentialBuildService] that returns a pre-supplied mock [ManagedIdentityCredential].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockManagedIdentityCredentialBuildService : ManagedIdentityCredentialBuildService() {
    override fun createClient(): ManagedIdentityCredential =
        checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: ManagedIdentityCredential? = null
    }
}
```

- [ ] **Step 5.4: Compile check**

```bash
./gradlew :azure-managed-identity-base:compileTestKotlin
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5.5: Commit**

```bash
git add cores/azure-managed-identity-base/src/
git commit -m "test(azure-managed-identity-base): add mock build services and ProviderFactoryExtensions"
```

---

### Task 6: IMDS ValueSources

**Files:**
- Create: `cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/AzureComputeMetadataValueSource.kt`
- Create: `cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/GetManagedIdentityInfoValueSource.kt`
- Create: `cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/AzureAttestedDataValueSource.kt`

- [ ] **Step 6.1: Write failing tests for all three IMDS ValueSources**

```kotlin
// cores/azure-managed-identity-base/src/test/kotlin/com/kelvsyc/gradle/azure/identity/AzureComputeMetadataValueSourceSpec.kt
package com.kelvsyc.gradle.azure.identity

import com.kelvsyc.gradle.azure.identity.model.AzureComputeMetadata
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldNotContainKey
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import retrofit2.Call
import retrofit2.Response

class AzureComputeMetadataValueSourceSpec : FunSpec() {
    init {
        test("obtain - maps non-null fields to string map") {
            val project = ProjectBuilder.builder().build()
            val imdsService = mockk<AzureImdsService>()
            MockAzureImdsClientBuildService.mockClient = imdsService
            val service = project.gradle.sharedServices
                .registerIfAbsent("imds", MockAzureImdsClientBuildService::class)

            val metadata = AzureComputeMetadata(
                subscriptionId = "sub-123",
                resourceGroupName = "rg-test",
                name = "my-vm",
                location = "eastus",
                vmId = "vm-id-456",
                vmSize = "Standard_D2s_v3",
                osType = "Linux",
            )
            val call = mockk<Call<AzureComputeMetadata>>()
            every { call.execute() } returns Response.success(metadata)
            every { imdsService.getComputeMetadata(any()) } returns call

            val provider = project.providers.ofKt(AzureComputeMetadataValueSource::class) {
                parameters.service.set(service)
                parameters.apiVersion.set("2021-02-01")
            }
            val result = provider.get()

            result shouldContain ("subscriptionId" to "sub-123")
            result shouldContain ("resourceGroupName" to "rg-test")
            result shouldContain ("name" to "my-vm")
            result shouldContain ("location" to "eastus")
            result shouldContain ("vmId" to "vm-id-456")
            result shouldContain ("vmSize" to "Standard_D2s_v3")
            result shouldContain ("osType" to "Linux")
        }

        test("obtain - omits null fields from map") {
            val project = ProjectBuilder.builder().build()
            val imdsService = mockk<AzureImdsService>()
            MockAzureImdsClientBuildService.mockClient = imdsService
            val service = project.gradle.sharedServices
                .registerIfAbsent("imds-partial", MockAzureImdsClientBuildService::class)

            val metadata = AzureComputeMetadata(
                subscriptionId = "sub-123",
                name = "my-vm",
            )
            val call = mockk<Call<AzureComputeMetadata>>()
            every { call.execute() } returns Response.success(metadata)
            every { imdsService.getComputeMetadata(any()) } returns call

            val provider = project.providers.ofKt(AzureComputeMetadataValueSource::class) {
                parameters.service.set(service)
                parameters.apiVersion.set("2021-02-01")
            }
            val result = provider.get()

            result shouldContain ("subscriptionId" to "sub-123")
            result shouldNotContainKey "location"
        }
    }
}
```

```kotlin
// cores/azure-managed-identity-base/src/test/kotlin/com/kelvsyc/gradle/azure/identity/GetManagedIdentityInfoValueSourceSpec.kt
package com.kelvsyc.gradle.azure.identity

import com.kelvsyc.gradle.azure.identity.model.AzureManagedIdentityInfo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import retrofit2.Call
import retrofit2.Response

class GetManagedIdentityInfoValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns clientId and objectId map") {
            val project = ProjectBuilder.builder().build()
            val imdsService = mockk<AzureImdsService>()
            MockAzureImdsClientBuildService.mockClient = imdsService
            val service = project.gradle.sharedServices
                .registerIfAbsent("imds", MockAzureImdsClientBuildService::class)

            val info = AzureManagedIdentityInfo(
                clientId = "client-abc",
                objectId = "object-xyz",
            )
            val call = mockk<Call<AzureManagedIdentityInfo>>()
            every { call.execute() } returns Response.success(info)
            every { imdsService.getManagedIdentityInfo(any()) } returns call

            val provider = project.providers.ofKt(GetManagedIdentityInfoValueSource::class) {
                parameters.service.set(service)
                parameters.apiVersion.set("2018-02-01")
            }
            val result = provider.get()

            result shouldContain ("clientId" to "client-abc")
            result shouldContain ("objectId" to "object-xyz")
        }
    }
}
```

```kotlin
// cores/azure-managed-identity-base/src/test/kotlin/com/kelvsyc/gradle/azure/identity/AzureAttestedDataValueSourceSpec.kt
package com.kelvsyc.gradle.azure.identity

import com.kelvsyc.gradle.azure.identity.model.AzureAttestedData
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import retrofit2.Call
import retrofit2.Response

class AzureAttestedDataValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns signature string") {
            val project = ProjectBuilder.builder().build()
            val imdsService = mockk<AzureImdsService>()
            MockAzureImdsClientBuildService.mockClient = imdsService
            val service = project.gradle.sharedServices
                .registerIfAbsent("imds", MockAzureImdsClientBuildService::class)

            val data = AzureAttestedData(encoding = "pkcs7", signature = "base64sig==")
            val call = mockk<Call<AzureAttestedData>>()
            every { call.execute() } returns Response.success(data)
            every { imdsService.getAttestedData(any(), any()) } returns call

            val provider = project.providers.ofKt(AzureAttestedDataValueSource::class) {
                parameters.service.set(service)
                parameters.apiVersion.set("2021-02-01")
            }
            val result = provider.get()

            result shouldBe "base64sig=="
        }

        test("obtain - passes nonce when set") {
            val project = ProjectBuilder.builder().build()
            val imdsService = mockk<AzureImdsService>()
            MockAzureImdsClientBuildService.mockClient = imdsService
            val service = project.gradle.sharedServices
                .registerIfAbsent("imds-nonce", MockAzureImdsClientBuildService::class)

            val nonceSlot = slot<String?>()
            val data = AzureAttestedData(encoding = "pkcs7", signature = "sig==")
            val call = mockk<Call<AzureAttestedData>>()
            every { call.execute() } returns Response.success(data)
            every { imdsService.getAttestedData(any(), capture(nonceSlot)) } returns call

            val provider = project.providers.ofKt(AzureAttestedDataValueSource::class) {
                parameters.service.set(service)
                parameters.apiVersion.set("2021-02-01")
                parameters.nonce.set("abc123")
            }
            provider.get()

            nonceSlot.captured shouldBe "abc123"
        }
    }
}
```

- [ ] **Step 6.2: Run failing tests**

```bash
./gradlew :azure-managed-identity-base:test --tests "com.kelvsyc.gradle.azure.identity.AzureComputeMetadataValueSourceSpec" --tests "com.kelvsyc.gradle.azure.identity.GetManagedIdentityInfoValueSourceSpec" --tests "com.kelvsyc.gradle.azure.identity.AzureAttestedDataValueSourceSpec" 2>&1 | tail -20
```

Expected: FAIL — classes not found.

- [ ] **Step 6.3: Implement `AzureComputeMetadataValueSource`**

```kotlin
// cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/AzureComputeMetadataValueSource.kt
package com.kelvsyc.gradle.azure.identity

import com.kelvsyc.gradle.azure.identity.model.AzureComputeMetadata
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that queries the Azure IMDS `/instance/compute` endpoint and returns a
 * [Map] of non-null fields from [AzureComputeMetadata].
 *
 * Keys match the JSON field names: `subscriptionId`, `resourceGroupName`, `name`, `location`,
 * `vmId`, `vmSize`, `osType`.
 */
abstract class AzureComputeMetadataValueSource :
    ValueSource<Map<String, String>, AzureComputeMetadataValueSource.Parameters> {

    /**
     * Parameters for [AzureComputeMetadataValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the IMDS client. */
        @get:Internal
        val service: Property<AzureImdsClientBuildService>

        /** IMDS API version to use. Defaults to `2021-02-01`. */
        val apiVersion: Property<String>
    }

    override fun obtain(): Map<String, String>? {
        val version = parameters.apiVersion.getOrElse(DEFAULT_API_VERSION)
        val response = parameters.service.get().getClient()
            .getComputeMetadata(version)
            .execute()
            .body() ?: return null

        return buildMap(response)
    }

    private fun buildMap(metadata: AzureComputeMetadata): Map<String, String> = buildMap {
        metadata.subscriptionId?.let { put("subscriptionId", it) }
        metadata.resourceGroupName?.let { put("resourceGroupName", it) }
        metadata.name?.let { put("name", it) }
        metadata.location?.let { put("location", it) }
        metadata.vmId?.let { put("vmId", it) }
        metadata.vmSize?.let { put("vmSize", it) }
        metadata.osType?.let { put("osType", it) }
    }

    private companion object {
        private const val DEFAULT_API_VERSION = "2021-02-01"
    }
}
```

- [ ] **Step 6.4: Implement `GetManagedIdentityInfoValueSource`**

```kotlin
// cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/GetManagedIdentityInfoValueSource.kt
package com.kelvsyc.gradle.azure.identity

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that queries the Azure IMDS `/identity/info` endpoint and returns a [Map]
 * with keys `clientId` and `objectId`.
 */
abstract class GetManagedIdentityInfoValueSource :
    ValueSource<Map<String, String>, GetManagedIdentityInfoValueSource.Parameters> {

    /**
     * Parameters for [GetManagedIdentityInfoValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the IMDS client. */
        @get:Internal
        val service: Property<AzureImdsClientBuildService>

        /** IMDS API version to use. Defaults to `2018-02-01`. */
        val apiVersion: Property<String>
    }

    override fun obtain(): Map<String, String>? {
        val version = parameters.apiVersion.getOrElse(DEFAULT_API_VERSION)
        val info = parameters.service.get().getClient()
            .getManagedIdentityInfo(version)
            .execute()
            .body() ?: return null

        return mapOf("clientId" to info.clientId, "objectId" to info.objectId)
    }

    private companion object {
        private const val DEFAULT_API_VERSION = "2018-02-01"
    }
}
```

- [ ] **Step 6.5: Implement `AzureAttestedDataValueSource`**

```kotlin
// cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/AzureAttestedDataValueSource.kt
package com.kelvsyc.gradle.azure.identity

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that queries the Azure IMDS `/attested/document` endpoint and returns the
 * raw `signature` string from the response.
 */
abstract class AzureAttestedDataValueSource :
    ValueSource<String, AzureAttestedDataValueSource.Parameters> {

    /**
     * Parameters for [AzureAttestedDataValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the IMDS client. */
        @get:Internal
        val service: Property<AzureImdsClientBuildService>

        /** IMDS API version to use. */
        val apiVersion: Property<String>

        /** Optional nonce for replay-attack prevention. */
        val nonce: Property<String>
    }

    override fun obtain(): String? {
        val version = parameters.apiVersion.get()
        val nonce = parameters.nonce.orNull
        val data = parameters.service.get().getClient()
            .getAttestedData(version, nonce)
            .execute()
            .body() ?: return null

        return data.signature
    }
}
```

- [ ] **Step 6.6: Run passing tests**

```bash
./gradlew :azure-managed-identity-base:test --tests "com.kelvsyc.gradle.azure.identity.AzureComputeMetadataValueSourceSpec" --tests "com.kelvsyc.gradle.azure.identity.GetManagedIdentityInfoValueSourceSpec" --tests "com.kelvsyc.gradle.azure.identity.AzureAttestedDataValueSourceSpec"
```

Expected: BUILD SUCCESSFUL, all tests pass.

- [ ] **Step 6.7: Commit**

```bash
git add cores/azure-managed-identity-base/src/
git commit -m "feat(azure-managed-identity-base): add IMDS ValueSources"
```

---

### Task 7: `AccessTokenValueSource`

**Files:**
- Create: `cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/AccessTokenValueSource.kt`

- [ ] **Step 7.1: Write the failing test**

```kotlin
// cores/azure-managed-identity-base/src/test/kotlin/com/kelvsyc/gradle/azure/identity/AccessTokenValueSourceSpec.kt
package com.kelvsyc.gradle.azure.identity

import com.azure.core.credential.AccessToken
import com.azure.core.credential.TokenRequestContext
import com.azure.identity.ManagedIdentityCredential
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import reactor.core.publisher.Mono
import java.time.OffsetDateTime

class AccessTokenValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns token string for requested scopes") {
            val project = ProjectBuilder.builder().build()
            val credential = mockk<ManagedIdentityCredential>()
            MockManagedIdentityCredentialBuildService.mockClient = credential
            val service = project.gradle.sharedServices
                .registerIfAbsent("mi", MockManagedIdentityCredentialBuildService::class)

            val contextSlot = slot<TokenRequestContext>()
            val accessToken = AccessToken("my-token-value", OffsetDateTime.now().plusHours(1))
            every { credential.getToken(capture(contextSlot)) } returns Mono.just(accessToken)

            val provider = project.providers.ofKt(AccessTokenValueSource::class) {
                parameters.service.set(service)
                parameters.scopes.add("https://management.azure.com/.default")
            }
            val result = provider.get()

            result shouldBe "my-token-value"
            contextSlot.captured.scopes shouldBe listOf("https://management.azure.com/.default")
        }
    }
}
```

- [ ] **Step 7.2: Run failing test**

```bash
./gradlew :azure-managed-identity-base:test --tests "com.kelvsyc.gradle.azure.identity.AccessTokenValueSourceSpec" 2>&1 | tail -20
```

Expected: FAIL — class not found.

- [ ] **Step 7.3: Implement `AccessTokenValueSource`**

```kotlin
// cores/azure-managed-identity-base/src/main/kotlin/com/kelvsyc/gradle/azure/identity/AccessTokenValueSource.kt
package com.kelvsyc.gradle.azure.identity

import com.azure.core.credential.TokenRequestContext
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that obtains an OAuth2 access token for the specified scopes using a
 * [ManagedIdentityCredentialBuildService].
 *
 * The returned string is the raw bearer token value. Obtain a scoped token for Azure Resource
 * Manager via scope `https://management.azure.com/.default`.
 */
abstract class AccessTokenValueSource : ValueSource<String, AccessTokenValueSource.Parameters> {

    /**
     * Parameters for [AccessTokenValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the managed identity credential. */
        @get:Internal
        val service: Property<ManagedIdentityCredentialBuildService>

        /** OAuth2 scopes to request. At least one scope must be provided. */
        val scopes: ListProperty<String>
    }

    override fun obtain(): String? {
        val context = TokenRequestContext().addScopes(*parameters.scopes.get().toTypedArray())
        return parameters.service.get().getClient().getToken(context).block()?.token
    }
}
```

- [ ] **Step 7.4: Run passing test**

```bash
./gradlew :azure-managed-identity-base:test --tests "com.kelvsyc.gradle.azure.identity.AccessTokenValueSourceSpec"
```

Expected: BUILD SUCCESSFUL, 1 test passed.

- [ ] **Step 7.5: Run all tests**

```bash
./gradlew :azure-managed-identity-base:test
```

Expected: BUILD SUCCESSFUL, all tests pass.

- [ ] **Step 7.6: Commit**

```bash
git add cores/azure-managed-identity-base/src/
git commit -m "feat(azure-managed-identity-base): add AccessTokenValueSource"
```

---

### Task 8: Detekt

- [ ] **Step 8.1: Run detekt**

```bash
./gradlew :azure-managed-identity-base:detekt
```

- [ ] **Step 8.2: Fix any detekt violations**

Common issues to watch for:
- `MagicNumber` — all API version string constants must be in `companion object { private const val ... }` blocks (already done in steps 6.3 and 6.4)
- `WildcardImport` — no wildcard imports
- `ForbiddenComment` — no TODO/FIXME/STOPSHIP markers
- `ReturnCount` — each function has ≤ 2 `return` statements

If any violation appears, fix it, then re-run:

```bash
./gradlew :azure-managed-identity-base:detekt
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 8.3: Commit if any fixes were needed**

```bash
git add cores/azure-managed-identity-base/src/
git commit -m "fix(azure-managed-identity-base): detekt violations"
```

---

### Task 9: Write component README

**Files:**
- Create: `cores/azure-managed-identity-base/README.md`

- [ ] **Step 9.1: Write README**

```markdown
# Azure Managed Identity Base

A Kotlin library providing managed Azure Managed Identity credential and Azure Instance Metadata
Service (IMDS) client integration using the Azure SDK for Java and Retrofit, built on `clients-base`.

## Dependency

\`\`\`kotlin
dependencies {
    implementation("com.kelvsyc.gradle:azure-managed-identity-base")
}
\`\`\`

## Build Services

| Class | Client type | Use case |
|---|---|---|
| `ManagedIdentityCredentialBuildService` | `ManagedIdentityCredential` | Obtain OAuth2 tokens for Azure resources |
| `AzureImdsClientBuildService` | `AzureImdsService` (Retrofit) | Query Azure IMDS endpoints |

### `ManagedIdentityCredentialBuildService`

Wraps the Azure Identity SDK's `ManagedIdentityCredential`. Use the extension functions on its
`Params` to select system-assigned or user-assigned identity:

\`\`\`kotlin
val mi = gradle.sharedServices.registerIfAbsent("mi", ManagedIdentityCredentialBuildService::class) {
    parameters.systemAssigned()  // no-op — system-assigned is the default
    // parameters.userAssigned("00000000-0000-0000-0000-000000000000")
    // parameters.userAssignedByObjectId("...")
    // parameters.userAssignedByResourceId("/subscriptions/.../resourceGroups/.../providers/...")
}
\`\`\`

### `AzureImdsClientBuildService`

Creates a Retrofit client for the Azure Instance Metadata Service. No parameters are needed;
the IMDS endpoint is fixed at `http://169.254.169.254/metadata/`.

\`\`\`kotlin
val imds = gradle.sharedServices.registerIfAbsent("imds", AzureImdsClientBuildService::class) {}
\`\`\`

## Value Sources

### `AccessTokenValueSource`

Retrieves a raw OAuth2 bearer token for the specified Azure scopes.

\`\`\`kotlin
val token: Provider<String> = providers.of(AccessTokenValueSource::class) {
    parameters {
        service.set(mi)
        scopes.add("https://management.azure.com/.default")
    }
}
\`\`\`

### `AzureComputeMetadataValueSource`

Queries `/instance/compute` and returns a `Map<String, String>` of non-null compute metadata fields
(`subscriptionId`, `resourceGroupName`, `name`, `location`, `vmId`, `vmSize`, `osType`).

\`\`\`kotlin
val compute: Provider<Map<String, String>> = providers.of(AzureComputeMetadataValueSource::class) {
    parameters {
        service.set(imds)
        apiVersion.set("2021-02-01")  // optional; defaults to 2021-02-01
    }
}
\`\`\`

### `GetManagedIdentityInfoValueSource`

Queries `/identity/info` and returns a `Map<String, String>` with keys `clientId` and `objectId`.

\`\`\`kotlin
val identityInfo: Provider<Map<String, String>> = providers.of(GetManagedIdentityInfoValueSource::class) {
    parameters {
        service.set(imds)
        apiVersion.set("2018-02-01")  // optional; defaults to 2018-02-01
    }
}
\`\`\`

### `AzureAttestedDataValueSource`

Queries `/attested/document` and returns the raw `signature` string (base64-encoded PKCS#7 document).

\`\`\`kotlin
val signature: Provider<String> = providers.of(AzureAttestedDataValueSource::class) {
    parameters {
        service.set(imds)
        apiVersion.set("2021-02-01")
        nonce.set("optional-nonce")  // optional
    }
}
\`\`\`

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [azure-blob-storage-base](../azure-blob-storage-base) — Azure Blob Storage variant
- [azure-key-vault-base](../azure-key-vault-base) — Azure Key Vault variant
```

- [ ] **Step 9.2: Commit**

```bash
git add cores/azure-managed-identity-base/README.md
git commit -m "docs(azure-managed-identity-base): add component README"
```

---

### Task 10: Update root README and final verification

**Files:**
- Modify: `README.md` (add `azure-managed-identity-base` to the "Other bases" table)

- [ ] **Step 10.1: Add row to "Other bases" table in `README.md`**

Find the table that begins with `| google-cloud-artifact-registry-base |` and add a new row after the `azure-key-vault-base` row:

```
| `azure-managed-identity-base` | Azure Managed Identity / IMDS | library |
```

The complete "Other bases" table after the edit:

```markdown
| Component | Description | Form |
|-----------|-------------|------|
| `google-cloud-artifact-registry-base` | GCP Artifact Registry | library |
| `google-cloud-storage-base` | GCP Cloud Storage | library |
| `google-cloud-secret-manager-base` | GCP Secret Manager | library |
| `google-cloud-pubsub-base` | GCP Pub/Sub | library |
| `google-cloud-kms-base` | GCP Cloud KMS | library |
| `azure-blob-storage-base` | Azure Blob Storage | library |
| `azure-key-vault-base` | Azure Key Vault | library |
| `azure-managed-identity-base` | Azure Managed Identity / IMDS | library |
| `artifactory-base` | JFrog Artifactory | library |
| `bitbucket-cloud-base` | Bitbucket Cloud REST API | library |
| `bitbucket-data-center-base` | Bitbucket Data Center REST API | library |
```

- [ ] **Step 10.2: Run full component build**

```bash
./gradlew :azure-managed-identity-base:build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 10.3: Run root aggregate build**

```bash
./gradlew :build
```

Expected: BUILD SUCCESSFUL — confirms BOM/catalog auto-discovery picked up the new component.

- [ ] **Step 10.4: Commit**

```bash
git add README.md
git commit -m "docs: add azure-managed-identity-base to root README bases table"
```

---

## Verification checklist

```bash
./gradlew :azure-managed-identity-base:test      # all unit tests pass
./gradlew :azure-managed-identity-base:detekt    # lint clean
./gradlew :azure-managed-identity-base:build     # full build
./gradlew :build                                  # root aggregate still builds
```
