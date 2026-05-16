# Vault WorkAction Test Coverage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add unit test coverage for all 7 untested WorkActions in `cores/hashicorp-vault-base`, and update `CLAUDE.md` to make WorkAction test coverage a documented requirement.

**Architecture:** Every tested WorkAction in this project uses a `MockXyzClientBuildService` that overrides `createClient()` to return a pre-injected mock client, bypassing real network connections. The action is instantiated as an anonymous subclass, its `getParameters()` is overridden to return a test `Parameters` instance, and `execute()` is called directly. The Vault module follows this pattern, with `MockVaultClientBuildService` returning a mocked `io.github.jopenlibs.vault.Vault`. For concrete actions (`WriteKvSecretAction`, `DeleteKvSecretAction`, `RevokeLeaseAction`), mock `vault.logical()` and `vault.leases()` directly. For abstract credential actions, also mock `logical.read(...)` to return a `LogicalResponse` with the right data map.

**Tech Stack:** Kotlin, Kotest FunSpec, mockk, Gradle TestFixtures, `io.github.jopenlibs:vault-java-driver:6.2.1`

---

## File Map

| File | Action | Responsibility |
|------|--------|---------------|
| `cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/MockVaultClientBuildService.kt` | CREATE | Shared test fixture — provides a mocked `Vault` client to all specs |
| `cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/WriteKvSecretActionSpec.kt` | CREATE | Verifies path and key/value map are passed to `Logical.write` |
| `cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/DeleteKvSecretActionSpec.kt` | CREATE | Verifies path is passed to `Logical.delete` |
| `cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/RevokeLeaseActionSpec.kt` | CREATE | Verifies lease ID is passed to `Leases.revoke` |
| `cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/AbstractAwsCredentialWorkActionSpec.kt` | CREATE | Verifies credential fields and exception propagation |
| `cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/AbstractAzureCredentialWorkActionSpec.kt` | CREATE | Same pattern, Azure fields |
| `cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/AbstractDatabaseCredentialWorkActionSpec.kt` | CREATE | Same pattern, database fields |
| `cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/AbstractGcpCredentialWorkActionSpec.kt` | CREATE | Same pattern, GCP token field |
| `CLAUDE.md` | MODIFY | Add explicit WorkAction test coverage requirement |

---

### Task 1: Create test directory and `MockVaultClientBuildService`

**Files:**
- Create: `cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/MockVaultClientBuildService.kt`

- [ ] **Step 1: Create the test source directory**

```bash
mkdir -p cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault
```

- [ ] **Step 2: Write `MockVaultClientBuildService.kt`**

```kotlin
package com.kelvsyc.gradle.hashicorp.vault

import io.github.jopenlibs.vault.Vault

/**
 * Test-only [VaultClientBuildService] that returns a pre-supplied mock [Vault] client.
 *
 * Set [mockClient] before the service is first accessed; the same instance is returned on every call.
 */
abstract class MockVaultClientBuildService : VaultClientBuildService() {
    override fun createClient(): Vault = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: Vault? = null
    }
}
```

- [ ] **Step 3: Verify it compiles**

```bash
./gradlew :hashicorp-vault-base:testClasses
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/MockVaultClientBuildService.kt
git commit -m "test(hashicorp-vault-base): add MockVaultClientBuildService test fixture"
```

---

### Task 2: `WriteKvSecretActionSpec`

**Files:**
- Create: `cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/WriteKvSecretActionSpec.kt`

`WriteKvSecretAction.execute()` calls `service.getClient().logical().write(path, mapOf(key to value))`. The test mocks `Vault.logical()` → `Logical`, captures the path and data map, and verifies them.

- [ ] **Step 1: Write `WriteKvSecretActionSpec.kt`**

```kotlin
package com.kelvsyc.gradle.hashicorp.vault

import io.github.jopenlibs.vault.Vault
import io.github.jopenlibs.vault.api.Logical
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class WriteKvSecretActionSpec : FunSpec() {
    init {
        test("execute - passes path and key-value map to Logical.write") {
            val project = ProjectBuilder.builder().build()
            val logical = mockk<Logical>()
            val vault = mockk<Vault>()
            every { vault.logical() } returns logical
            val pathSlot = slot<String>()
            val dataSlot = slot<Map<String, Any?>>()
            every { logical.write(capture(pathSlot), capture(dataSlot)) } returns mockk(relaxed = true)

            MockVaultClientBuildService.mockClient = vault
            val service = project.gradle.sharedServices.registerIfAbsent("vault", MockVaultClientBuildService::class)

            val params = project.objects.newInstance<WriteKvSecretAction.Parameters>()
            params.service.set(service)
            params.path.set("secret/data/myapp")
            params.key.set("apiKey")
            params.value.set("supersecret")

            object : WriteKvSecretAction() {
                override fun getParameters() = params
            }.execute()

            pathSlot.captured shouldBe "secret/data/myapp"
            dataSlot.captured shouldBe mapOf("apiKey" to "supersecret")
        }
    }
}
```

- [ ] **Step 2: Run the test**

```bash
./gradlew :hashicorp-vault-base:test --tests "*.WriteKvSecretActionSpec"
```

Expected: `BUILD SUCCESSFUL`, 1 test passed.

- [ ] **Step 3: Commit**

```bash
git add cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/WriteKvSecretActionSpec.kt
git commit -m "test(hashicorp-vault-base): add WriteKvSecretAction unit tests"
```

---

### Task 3: `DeleteKvSecretActionSpec`

**Files:**
- Create: `cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/DeleteKvSecretActionSpec.kt`

`DeleteKvSecretAction.execute()` calls `service.getClient().logical().delete(path)`. Capture and verify the path.

- [ ] **Step 1: Write `DeleteKvSecretActionSpec.kt`**

```kotlin
package com.kelvsyc.gradle.hashicorp.vault

import io.github.jopenlibs.vault.Vault
import io.github.jopenlibs.vault.api.Logical
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class DeleteKvSecretActionSpec : FunSpec() {
    init {
        test("execute - passes path to Logical.delete") {
            val project = ProjectBuilder.builder().build()
            val logical = mockk<Logical>()
            val vault = mockk<Vault>()
            every { vault.logical() } returns logical
            val pathSlot = slot<String>()
            every { logical.delete(capture(pathSlot)) } returns mockk(relaxed = true)

            MockVaultClientBuildService.mockClient = vault
            val service = project.gradle.sharedServices.registerIfAbsent("vault", MockVaultClientBuildService::class)

            val params = project.objects.newInstance<DeleteKvSecretAction.Parameters>()
            params.service.set(service)
            params.path.set("secret/data/myapp")

            object : DeleteKvSecretAction() {
                override fun getParameters() = params
            }.execute()

            pathSlot.captured shouldBe "secret/data/myapp"
        }
    }
}
```

- [ ] **Step 2: Run the test**

```bash
./gradlew :hashicorp-vault-base:test --tests "*.DeleteKvSecretActionSpec"
```

Expected: `BUILD SUCCESSFUL`, 1 test passed.

- [ ] **Step 3: Commit**

```bash
git add cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/DeleteKvSecretActionSpec.kt
git commit -m "test(hashicorp-vault-base): add DeleteKvSecretAction unit tests"
```

---

### Task 4: `RevokeLeaseActionSpec`

**Files:**
- Create: `cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/RevokeLeaseActionSpec.kt`

`RevokeLeaseAction.execute()` calls `service.revokeLease(leaseId)`, which internally calls `getClient().leases().revoke(leaseId)` inside `runCatching`. Mock `vault.leases()` → `Leases` and verify `revoke` is called with the correct lease ID.

Note: `Vault.leases()` is deprecated in vault-java-driver 6.2.1 (matching the `@Suppress("DEPRECATION")` in `AbstractVaultClientBuildService`). The test class needs the same suppression on any block that references it through the mockk DSL.

- [ ] **Step 1: Write `RevokeLeaseActionSpec.kt`**

```kotlin
package com.kelvsyc.gradle.hashicorp.vault

import io.github.jopenlibs.vault.Vault
import io.github.jopenlibs.vault.api.sys.Leases
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

@Suppress("DEPRECATION")
class RevokeLeaseActionSpec : FunSpec() {
    init {
        test("execute - calls revoke on the build service with the correct lease ID") {
            val project = ProjectBuilder.builder().build()
            val leases = mockk<Leases>(relaxed = true)
            val vault = mockk<Vault>()
            every { vault.leases() } returns leases

            MockVaultClientBuildService.mockClient = vault
            val service = project.gradle.sharedServices.registerIfAbsent("vault", MockVaultClientBuildService::class)

            val params = project.objects.newInstance<RevokeLeaseAction.Parameters>()
            params.service.set(service)
            params.leaseId.set("aws/creds/my-role/abc-123")

            object : RevokeLeaseAction() {
                override fun getParameters() = params
            }.execute()

            verify { leases.revoke("aws/creds/my-role/abc-123") }
        }
    }
}
```

- [ ] **Step 2: Run the test**

```bash
./gradlew :hashicorp-vault-base:test --tests "*.RevokeLeaseActionSpec"
```

Expected: `BUILD SUCCESSFUL`, 1 test passed.

- [ ] **Step 3: Commit**

```bash
git add cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/RevokeLeaseActionSpec.kt
git commit -m "test(hashicorp-vault-base): add RevokeLeaseAction unit tests"
```

---

### Task 5: `AbstractAwsCredentialWorkActionSpec`

**Files:**
- Create: `cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/AbstractAwsCredentialWorkActionSpec.kt`

`AbstractAwsCredentialWorkAction.execute()` calls `service.issueAwsCredential(role)` (which reads from `"aws/creds/$role"` via `Logical`) then calls `doExecute(credential)`, then revokes the lease in `finally`. Two tests:
1. Happy path: `doExecute` receives an `AwsDynamicCredential` with fields correctly mapped from `LogicalResponse.data` and `LogicalResponse.leaseId`.
2. Exception path: an exception thrown from `doExecute` propagates out of `execute()` (not swallowed by `finally`).

`issueAwsCredential` maps: `data["access_key"]` → `accessKeyId`, `data["secret_key"]` → `secretAccessKey`, `data["security_token"]` → `sessionToken` (nullable), `leaseId` → `leaseId`, `leaseDuration` (seconds, Long) → `leaseDuration` (Duration).

The `revokeLease` call uses `vault.leases()` (deprecated). Use `@Suppress("DEPRECATION")` and mock `vault.leases()` with a relaxed mock so revocation succeeds silently.

- [ ] **Step 1: Write `AbstractAwsCredentialWorkActionSpec.kt`**

```kotlin
package com.kelvsyc.gradle.hashicorp.vault

import io.github.jopenlibs.vault.Vault
import io.github.jopenlibs.vault.api.Logical
import io.github.jopenlibs.vault.api.sys.Leases
import io.github.jopenlibs.vault.response.LogicalResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

@Suppress("DEPRECATION")
class AbstractAwsCredentialWorkActionSpec : FunSpec() {
    private fun buildSetup(role: String, responseData: Map<String, String>, leaseId: String = "aws/creds/$role/abc-123"): Vault {
        val response = mockk<LogicalResponse>()
        every { response.leaseId } returns leaseId
        every { response.data } returns responseData
        every { response.leaseDuration } returns 3600L

        val logical = mockk<Logical>()
        every { logical.read("aws/creds/$role") } returns response

        val vault = mockk<Vault>()
        every { vault.logical() } returns logical
        every { vault.leases() } returns mockk<Leases>(relaxed = true)

        return vault
    }

    init {
        context("happy path") {
            test("execute - passes credential fields mapped from Vault response to doExecute") {
                val project = ProjectBuilder.builder().build()
                val vault = buildSetup(
                    role = "my-role",
                    responseData = mapOf(
                        "access_key" to "AKIAEXAMPLE",
                        "secret_key" to "wJalrXUtn",
                        "security_token" to "STStoken",
                    ),
                    leaseId = "aws/creds/my-role/abc-123",
                )

                MockVaultClientBuildService.mockClient = vault
                val service = project.gradle.sharedServices.registerIfAbsent("vault", MockVaultClientBuildService::class)

                val params = project.objects.newInstance<AbstractAwsCredentialWorkAction.Parameters>()
                params.service.set(service)
                params.role.set("my-role")

                var received: AwsDynamicCredential? = null
                object : AbstractAwsCredentialWorkAction() {
                    override fun getParameters() = params
                    override fun doExecute(credential: AwsDynamicCredential) { received = credential }
                }.execute()

                received!!.accessKeyId shouldBe "AKIAEXAMPLE"
                received!!.secretAccessKey shouldBe "wJalrXUtn"
                received!!.sessionToken shouldBe "STStoken"
                received!!.leaseId shouldBe "aws/creds/my-role/abc-123"
            }

            test("execute - sessionToken is null when security_token absent from response") {
                val project = ProjectBuilder.builder().build()
                val vault = buildSetup(
                    role = "no-sts-role",
                    responseData = mapOf("access_key" to "AKID", "secret_key" to "secret"),
                )

                MockVaultClientBuildService.mockClient = vault
                val service = project.gradle.sharedServices.registerIfAbsent("vault", MockVaultClientBuildService::class)

                val params = project.objects.newInstance<AbstractAwsCredentialWorkAction.Parameters>()
                params.service.set(service)
                params.role.set("no-sts-role")

                var received: AwsDynamicCredential? = null
                object : AbstractAwsCredentialWorkAction() {
                    override fun getParameters() = params
                    override fun doExecute(credential: AwsDynamicCredential) { received = credential }
                }.execute()

                received!!.sessionToken shouldBe null
            }
        }

        context("exception propagation") {
            test("execute - propagates exception from doExecute after revoking lease") {
                val project = ProjectBuilder.builder().build()
                val vault = buildSetup(
                    role = "my-role",
                    responseData = mapOf("access_key" to "AKID", "secret_key" to "secret"),
                )

                MockVaultClientBuildService.mockClient = vault
                val service = project.gradle.sharedServices.registerIfAbsent("vault", MockVaultClientBuildService::class)

                val params = project.objects.newInstance<AbstractAwsCredentialWorkAction.Parameters>()
                params.service.set(service)
                params.role.set("my-role")

                val action = object : AbstractAwsCredentialWorkAction() {
                    override fun getParameters() = params
                    override fun doExecute(credential: AwsDynamicCredential) {
                        throw IllegalStateException("work failed")
                    }
                }

                shouldThrow<IllegalStateException> { action.execute() }
            }
        }
    }
}
```

- [ ] **Step 2: Run the tests**

```bash
./gradlew :hashicorp-vault-base:test --tests "*.AbstractAwsCredentialWorkActionSpec"
```

Expected: `BUILD SUCCESSFUL`, 3 tests passed.

- [ ] **Step 3: Commit**

```bash
git add cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/AbstractAwsCredentialWorkActionSpec.kt
git commit -m "test(hashicorp-vault-base): add AbstractAwsCredentialWorkAction unit tests"
```

---

### Task 6: `AbstractAzureCredentialWorkActionSpec`

**Files:**
- Create: `cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/AbstractAzureCredentialWorkActionSpec.kt`

`issueAzureCredential` reads from `"azure/creds/$role"` and maps: `data["client_id"]` → `clientId`, `data["client_secret"]` → `clientSecret`. Same two-test structure as Task 5.

- [ ] **Step 1: Write `AbstractAzureCredentialWorkActionSpec.kt`**

```kotlin
package com.kelvsyc.gradle.hashicorp.vault

import io.github.jopenlibs.vault.Vault
import io.github.jopenlibs.vault.api.Logical
import io.github.jopenlibs.vault.api.sys.Leases
import io.github.jopenlibs.vault.response.LogicalResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

@Suppress("DEPRECATION")
class AbstractAzureCredentialWorkActionSpec : FunSpec() {
    private fun buildSetup(role: String): Vault {
        val response = mockk<LogicalResponse>()
        every { response.leaseId } returns "azure/creds/$role/abc-123"
        every { response.data } returns mapOf("client_id" to "azure-client-id", "client_secret" to "azure-secret")
        every { response.leaseDuration } returns 3600L

        val logical = mockk<Logical>()
        every { logical.read("azure/creds/$role") } returns response

        val vault = mockk<Vault>()
        every { vault.logical() } returns logical
        every { vault.leases() } returns mockk<Leases>(relaxed = true)
        return vault
    }

    init {
        test("execute - passes credential fields mapped from Vault response to doExecute") {
            val project = ProjectBuilder.builder().build()
            MockVaultClientBuildService.mockClient = buildSetup("my-role")
            val service = project.gradle.sharedServices.registerIfAbsent("vault", MockVaultClientBuildService::class)

            val params = project.objects.newInstance<AbstractAzureCredentialWorkAction.Parameters>()
            params.service.set(service)
            params.role.set("my-role")

            var received: AzureDynamicCredential? = null
            object : AbstractAzureCredentialWorkAction() {
                override fun getParameters() = params
                override fun doExecute(credential: AzureDynamicCredential) { received = credential }
            }.execute()

            received!!.clientId shouldBe "azure-client-id"
            received!!.clientSecret shouldBe "azure-secret"
            received!!.leaseId shouldBe "azure/creds/my-role/abc-123"
        }

        test("execute - propagates exception from doExecute") {
            val project = ProjectBuilder.builder().build()
            MockVaultClientBuildService.mockClient = buildSetup("my-role")
            val service = project.gradle.sharedServices.registerIfAbsent("vault", MockVaultClientBuildService::class)

            val params = project.objects.newInstance<AbstractAzureCredentialWorkAction.Parameters>()
            params.service.set(service)
            params.role.set("my-role")

            val action = object : AbstractAzureCredentialWorkAction() {
                override fun getParameters() = params
                override fun doExecute(credential: AzureDynamicCredential) {
                    throw IllegalStateException("work failed")
                }
            }

            shouldThrow<IllegalStateException> { action.execute() }
        }
    }
}
```

- [ ] **Step 2: Run the tests**

```bash
./gradlew :hashicorp-vault-base:test --tests "*.AbstractAzureCredentialWorkActionSpec"
```

Expected: `BUILD SUCCESSFUL`, 2 tests passed.

- [ ] **Step 3: Commit**

```bash
git add cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/AbstractAzureCredentialWorkActionSpec.kt
git commit -m "test(hashicorp-vault-base): add AbstractAzureCredentialWorkAction unit tests"
```

---

### Task 7: `AbstractDatabaseCredentialWorkActionSpec`

**Files:**
- Create: `cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/AbstractDatabaseCredentialWorkActionSpec.kt`

`issueDatabaseCredential` reads from `"database/creds/$role"` and maps: `data["username"]` → `username`, `data["password"]` → `password`.

- [ ] **Step 1: Write `AbstractDatabaseCredentialWorkActionSpec.kt`**

```kotlin
package com.kelvsyc.gradle.hashicorp.vault

import io.github.jopenlibs.vault.Vault
import io.github.jopenlibs.vault.api.Logical
import io.github.jopenlibs.vault.api.sys.Leases
import io.github.jopenlibs.vault.response.LogicalResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

@Suppress("DEPRECATION")
class AbstractDatabaseCredentialWorkActionSpec : FunSpec() {
    private fun buildSetup(role: String): Vault {
        val response = mockk<LogicalResponse>()
        every { response.leaseId } returns "database/creds/$role/abc-123"
        every { response.data } returns mapOf("username" to "app_user_xyz", "password" to "random-pw-abc")
        every { response.leaseDuration } returns 3600L

        val logical = mockk<Logical>()
        every { logical.read("database/creds/$role") } returns response

        val vault = mockk<Vault>()
        every { vault.logical() } returns logical
        every { vault.leases() } returns mockk<Leases>(relaxed = true)
        return vault
    }

    init {
        test("execute - passes credential fields mapped from Vault response to doExecute") {
            val project = ProjectBuilder.builder().build()
            MockVaultClientBuildService.mockClient = buildSetup("my-db-role")
            val service = project.gradle.sharedServices.registerIfAbsent("vault", MockVaultClientBuildService::class)

            val params = project.objects.newInstance<AbstractDatabaseCredentialWorkAction.Parameters>()
            params.service.set(service)
            params.role.set("my-db-role")

            var received: DatabaseCredential? = null
            object : AbstractDatabaseCredentialWorkAction() {
                override fun getParameters() = params
                override fun doExecute(credential: DatabaseCredential) { received = credential }
            }.execute()

            received!!.username shouldBe "app_user_xyz"
            received!!.password shouldBe "random-pw-abc"
            received!!.leaseId shouldBe "database/creds/my-db-role/abc-123"
        }

        test("execute - propagates exception from doExecute") {
            val project = ProjectBuilder.builder().build()
            MockVaultClientBuildService.mockClient = buildSetup("my-db-role")
            val service = project.gradle.sharedServices.registerIfAbsent("vault", MockVaultClientBuildService::class)

            val params = project.objects.newInstance<AbstractDatabaseCredentialWorkAction.Parameters>()
            params.service.set(service)
            params.role.set("my-db-role")

            val action = object : AbstractDatabaseCredentialWorkAction() {
                override fun getParameters() = params
                override fun doExecute(credential: DatabaseCredential) {
                    throw IllegalStateException("work failed")
                }
            }

            shouldThrow<IllegalStateException> { action.execute() }
        }
    }
}
```

- [ ] **Step 2: Run the tests**

```bash
./gradlew :hashicorp-vault-base:test --tests "*.AbstractDatabaseCredentialWorkActionSpec"
```

Expected: `BUILD SUCCESSFUL`, 2 tests passed.

- [ ] **Step 3: Commit**

```bash
git add cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/AbstractDatabaseCredentialWorkActionSpec.kt
git commit -m "test(hashicorp-vault-base): add AbstractDatabaseCredentialWorkAction unit tests"
```

---

### Task 8: `AbstractGcpCredentialWorkActionSpec`

**Files:**
- Create: `cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/AbstractGcpCredentialWorkActionSpec.kt`

`issueGcpCredential` reads from `"gcp/token/$role"` and maps: `data["token"]` → `token`.

- [ ] **Step 1: Write `AbstractGcpCredentialWorkActionSpec.kt`**

```kotlin
package com.kelvsyc.gradle.hashicorp.vault

import io.github.jopenlibs.vault.Vault
import io.github.jopenlibs.vault.api.Logical
import io.github.jopenlibs.vault.api.sys.Leases
import io.github.jopenlibs.vault.response.LogicalResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

@Suppress("DEPRECATION")
class AbstractGcpCredentialWorkActionSpec : FunSpec() {
    private fun buildSetup(role: String): Vault {
        val response = mockk<LogicalResponse>()
        every { response.leaseId } returns "gcp/token/$role/abc-123"
        every { response.data } returns mapOf("token" to "ya29.access-token-example")
        every { response.leaseDuration } returns 3600L

        val logical = mockk<Logical>()
        every { logical.read("gcp/token/$role") } returns response

        val vault = mockk<Vault>()
        every { vault.logical() } returns logical
        every { vault.leases() } returns mockk<Leases>(relaxed = true)
        return vault
    }

    init {
        test("execute - passes credential fields mapped from Vault response to doExecute") {
            val project = ProjectBuilder.builder().build()
            MockVaultClientBuildService.mockClient = buildSetup("my-gcp-role")
            val service = project.gradle.sharedServices.registerIfAbsent("vault", MockVaultClientBuildService::class)

            val params = project.objects.newInstance<AbstractGcpCredentialWorkAction.Parameters>()
            params.service.set(service)
            params.role.set("my-gcp-role")

            var received: GcpDynamicCredential? = null
            object : AbstractGcpCredentialWorkAction() {
                override fun getParameters() = params
                override fun doExecute(credential: GcpDynamicCredential) { received = credential }
            }.execute()

            received!!.token shouldBe "ya29.access-token-example"
            received!!.leaseId shouldBe "gcp/token/my-gcp-role/abc-123"
        }

        test("execute - propagates exception from doExecute") {
            val project = ProjectBuilder.builder().build()
            MockVaultClientBuildService.mockClient = buildSetup("my-gcp-role")
            val service = project.gradle.sharedServices.registerIfAbsent("vault", MockVaultClientBuildService::class)

            val params = project.objects.newInstance<AbstractGcpCredentialWorkAction.Parameters>()
            params.service.set(service)
            params.role.set("my-gcp-role")

            val action = object : AbstractGcpCredentialWorkAction() {
                override fun getParameters() = params
                override fun doExecute(credential: GcpDynamicCredential) {
                    throw IllegalStateException("work failed")
                }
            }

            shouldThrow<IllegalStateException> { action.execute() }
        }
    }
}
```

- [ ] **Step 2: Run the tests**

```bash
./gradlew :hashicorp-vault-base:test --tests "*.AbstractGcpCredentialWorkActionSpec"
```

Expected: `BUILD SUCCESSFUL`, 2 tests passed.

- [ ] **Step 3: Commit**

```bash
git add cores/hashicorp-vault-base/src/test/kotlin/com/kelvsyc/gradle/hashicorp/vault/AbstractGcpCredentialWorkActionSpec.kt
git commit -m "test(hashicorp-vault-base): add AbstractGcpCredentialWorkAction unit tests"
```

---

### Task 9: Update `CLAUDE.md`

**Files:**
- Modify: `CLAUDE.md`

Add an explicit WorkAction test coverage requirement to the Requirements section to prevent future agents from deferring tests on the grounds that WorkActions are hard to test.

- [ ] **Step 1: Edit `CLAUDE.md` — add the WorkAction test requirement**

In the `## Requirements` section, add after the KDocs requirement:

```
- **Every `WorkAction` implementation must have a unit test.** Follow the `MockXyzClientBuildService` pattern: create a test-only service subclass in `src/test/kotlin` that overrides `createClient()` to return a mock client (see `MockSecretsManagerClientBuildService` as reference). Instantiate the action as an anonymous subclass with `getParameters()` overridden, then call `execute()` directly. WorkActions that call external services (AWS, GCP, Azure, Vault) are fully unit-testable against mocked clients — do not defer tests on the grounds that integration tests are required.
```

- [ ] **Step 2: Run detekt to verify no lint issues**

```bash
./gradlew :detekt
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add CLAUDE.md
git commit -m "docs: require unit tests for all WorkAction implementations in CLAUDE.md"
```

---

### Task 10: Full verification

- [ ] **Step 1: Run the full hashicorp-vault-base test suite**

```bash
./gradlew :hashicorp-vault-base:test
```

Expected: `BUILD SUCCESSFUL`, 12 tests passed (1 + 1 + 1 + 3 + 2 + 2 + 2).

- [ ] **Step 2: Run detekt on the new test files**

```bash
./gradlew :hashicorp-vault-base:detekt
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Run full project tests**

```bash
./gradlew :test
```

Expected: `BUILD SUCCESSFUL`
