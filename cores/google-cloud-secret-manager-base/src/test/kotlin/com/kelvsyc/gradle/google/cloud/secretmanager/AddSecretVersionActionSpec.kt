package com.kelvsyc.gradle.google.cloud.secretmanager

import com.google.cloud.secretmanager.v1.AddSecretVersionRequest
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretVersion
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class AddSecretVersionActionSpec : FunSpec() {
    init {
        test("execute - calls addSecretVersion with correct parent and payload") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SecretManagerServiceClient>()
            MockSecretManagerServiceClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sm", MockSecretManagerServiceClientBuildService::class)

            val slot = slot<AddSecretVersionRequest>()
            every { client.addSecretVersion(capture(slot)) } returns mockk<SecretVersion>(relaxed = true)

            val params = project.objects.newInstance<AddSecretVersionAction.Parameters>()
            params.service.set(service)
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
