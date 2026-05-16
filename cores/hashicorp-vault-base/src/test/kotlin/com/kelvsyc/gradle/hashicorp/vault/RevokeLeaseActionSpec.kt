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
