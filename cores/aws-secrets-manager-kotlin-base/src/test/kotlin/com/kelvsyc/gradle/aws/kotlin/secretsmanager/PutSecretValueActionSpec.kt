package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import aws.sdk.kotlin.services.secretsmanager.model.PutSecretValueRequest
import aws.sdk.kotlin.services.secretsmanager.model.PutSecretValueResponse
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.secretsmanager.MockSecretsManagerClientInfoInternal
import com.kelvsyc.gradle.plugins.SecretsManagerKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class PutSecretValueActionSpec : FunSpec() {
    init {
        test("execute - passes correct secretId and secretString to Secrets Manager") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SecretsManagerKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSecretsManagerClientInfo::class, MockSecretsManagerClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSecretsManagerClientInfo>("mock") {}

            val client = extension.getClient<SecretsManagerClient, MockSecretsManagerClientInfo>("mock").get()!!
            val requestSlot = slot<PutSecretValueRequest>()
            coEvery { client.putSecretValue(capture(requestSlot)) } returns mockk<PutSecretValueResponse>()

            val params = project.objects.newInstance<PutSecretValueAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.secretId.set("my/secret/name")
            params.secretString.set("{\"username\":\"admin\",\"password\":\"s3cret\"}")

            val action = object : PutSecretValueAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.secretId shouldBe "my/secret/name"
            captured.secretString shouldBe "{\"username\":\"admin\",\"password\":\"s3cret\"}"
        }
    }
}
