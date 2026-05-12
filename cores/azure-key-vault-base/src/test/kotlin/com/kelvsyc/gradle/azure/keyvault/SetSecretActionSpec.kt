package com.kelvsyc.gradle.azure.keyvault

import com.azure.security.keyvault.secrets.SecretClient
import com.azure.security.keyvault.secrets.models.KeyVaultSecret
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.azure.keyvault.MockSecretClientInfoInternal
import com.kelvsyc.gradle.plugins.AzureKeyVaultBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class SetSecretActionSpec : FunSpec() {
    init {
        test("execute - calls setSecret with correct name and value") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(AzureKeyVaultBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSecretClientInfo::class, MockSecretClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSecretClientInfo>("mock") {}

            val client = extension.getClient<SecretClient, MockSecretClientInfo>("mock").get()!!
            val nameSlot = slot<String>()
            val valueSlot = slot<String>()
            every { client.setSecret(capture(nameSlot), capture(valueSlot)) } returns mockk<KeyVaultSecret>()

            val params = project.objects.newInstance<SetSecretAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
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
