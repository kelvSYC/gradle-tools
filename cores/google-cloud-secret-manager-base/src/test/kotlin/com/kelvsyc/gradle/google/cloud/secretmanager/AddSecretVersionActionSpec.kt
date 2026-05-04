package com.kelvsyc.gradle.google.cloud.secretmanager

import com.google.cloud.secretmanager.v1.AddSecretVersionRequest
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretVersion
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.google.cloud.secretmanager.MockSecretManagerClientInfoInternal
import com.kelvsyc.gradle.plugins.GoogleCloudSecretManagerBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class AddSecretVersionActionSpec : FunSpec() {
    init {
        test("execute - calls addSecretVersion with correct parent and payload") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(GoogleCloudSecretManagerBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSecretManagerClientInfo::class, MockSecretManagerClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSecretManagerClientInfo>("mock") {}

            val client = extension.getClient<SecretManagerServiceClient, MockSecretManagerClientInfo>("mock").get()!!
            val slot = slot<AddSecretVersionRequest>()
            every { client.addSecretVersion(capture(slot)) } returns mockk<SecretVersion>(relaxed = true)

            val params = project.objects.newInstance<AddSecretVersionAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.projectId.set("my-project")
            params.secretId.set("my-secret")
            params.payload.set("new-secret-value")

            val action = object : AddSecretVersionAction() {
                override fun getParameters() = params
            }
            action.execute()

            slot.captured.parent shouldContain "my-project"
            slot.captured.parent shouldContain "my-secret"
            slot.captured.payload.data.toStringUtf8() shouldBe "new-secret-value"
        }
    }
}
