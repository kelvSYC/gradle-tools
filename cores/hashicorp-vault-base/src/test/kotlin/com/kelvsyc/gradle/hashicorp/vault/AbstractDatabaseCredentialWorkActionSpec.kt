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

            received?.username shouldBe "app_user_xyz"
            received?.password shouldBe "random-pw-abc"
            received?.leaseId shouldBe "database/creds/my-db-role/abc-123"
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
                    error("work failed")
                }
            }

            shouldThrow<IllegalStateException> { action.execute() }
        }
    }
}
