package com.kelvsyc.gradle.azure.keyvault

import com.azure.security.keyvault.secrets.SecretClient
import com.azure.security.keyvault.secrets.models.KeyVaultSecret
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class SetSecretActionSpec : FunSpec() {
    init {
        test("execute - calls setSecret with correct name and value") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SecretClient>()
            MockSecretClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("kv", MockSecretClientBuildService::class)

            val nameSlot = slot<String>()
            val valueSlot = slot<String>()
            every { client.setSecret(capture(nameSlot), capture(valueSlot)) } returns mockk<KeyVaultSecret>()

            val params = project.objects.newInstance<SetSecretAction.Parameters>()
            params.service.set(service)
            params.secretName.set("my-secret")
            params.secretValue.set("{\"username\":\"admin\",\"password\":\"s3cret\"}")

            val action = object : SetSecretAction() {
                override fun getParameters() = params
            }
            action.execute()

            nameSlot.captured shouldBe "my-secret"
            valueSlot.captured shouldBe "{\"username\":\"admin\",\"password\":\"s3cret\"}"
        }
    }
}
