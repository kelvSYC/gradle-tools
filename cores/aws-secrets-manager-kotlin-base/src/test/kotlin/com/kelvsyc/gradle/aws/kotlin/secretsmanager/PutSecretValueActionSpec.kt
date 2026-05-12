package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import aws.sdk.kotlin.services.secretsmanager.model.PutSecretValueRequest
import aws.sdk.kotlin.services.secretsmanager.model.PutSecretValueResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class PutSecretValueActionSpec : FunSpec() {
    init {
        test("execute - passes correct secretId and secretString to Secrets Manager") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SecretsManagerClient>()
            MockSecretsManagerClientBuildService.mockClient = client
            val service =
                project.gradle.sharedServices.registerIfAbsent("sm", MockSecretsManagerClientBuildService::class)
            val requestSlot = slot<PutSecretValueRequest>()
            coEvery { client.putSecretValue(capture(requestSlot)) } returns mockk<PutSecretValueResponse>()

            val params = project.objects.newInstance<PutSecretValueAction.Parameters>()
            params.service.set(service)
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
