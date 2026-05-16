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
