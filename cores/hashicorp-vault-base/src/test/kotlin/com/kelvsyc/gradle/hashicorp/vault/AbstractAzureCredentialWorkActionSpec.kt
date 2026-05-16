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

            received?.clientId shouldBe "azure-client-id"
            received?.clientSecret shouldBe "azure-secret"
            received?.leaseId shouldBe "azure/creds/my-role/abc-123"
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
