package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import aws.sdk.kotlin.services.secretsmanager.model.PutSecretValueRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class PutSecretValueSpec : FunSpec({
    test("execute sends correct secretId and secretString to Secrets Manager") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<SecretsManagerClient>()
        MockSecretsManagerClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("sm", MockSecretsManagerClientBuildService::class)
        val requestSlot = slot<PutSecretValueRequest>()
        coEvery { client.putSecretValue(capture(requestSlot)) } returns mockk()

        val task = project.tasks.create("t", PutSecretValue::class.java)
        task.service.set(service)
        task.secretId.set("my/secret/name")
        task.secretString.set("{\"username\":\"admin\",\"password\":\"s3cret\"}")

        task.execute()

        val captured = requestSlot.captured
        captured.secretId shouldBe "my/secret/name"
        captured.secretString shouldBe "{\"username\":\"admin\",\"password\":\"s3cret\"}"
        MockSecretsManagerClientBuildService.mockClient = null
    }

    test("execute verifies service client call is made") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<SecretsManagerClient>()
        MockSecretsManagerClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("sm2", MockSecretsManagerClientBuildService::class)
        coEvery { client.putSecretValue(any()) } returns mockk()

        val task = project.tasks.create("t2", PutSecretValue::class.java)
        task.service.set(service)
        task.secretId.set("test/secret")
        task.secretString.set("test-value")

        task.execute()

        coVerify { client.putSecretValue(any()) }
        MockSecretsManagerClientBuildService.mockClient = null
    }
})
