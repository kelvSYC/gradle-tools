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

            received?.token shouldBe "ya29.access-token-example"
            received?.leaseId shouldBe "gcp/token/my-gcp-role/abc-123"
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
                    error("work failed")
                }
            }

            shouldThrow<IllegalStateException> { action.execute() }
        }
    }
}
