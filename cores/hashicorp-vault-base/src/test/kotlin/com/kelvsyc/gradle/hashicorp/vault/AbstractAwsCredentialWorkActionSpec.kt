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

                received?.accessKeyId shouldBe "AKIAEXAMPLE"
                received?.secretAccessKey shouldBe "wJalrXUtn"
                received?.sessionToken shouldBe "STStoken"
                received?.leaseId shouldBe "aws/creds/my-role/abc-123"
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

                received?.sessionToken shouldBe null
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
