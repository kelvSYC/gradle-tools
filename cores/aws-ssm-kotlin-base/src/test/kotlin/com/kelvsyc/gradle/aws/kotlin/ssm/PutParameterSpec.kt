package com.kelvsyc.gradle.aws.kotlin.ssm

import aws.sdk.kotlin.services.ssm.SsmClient
import aws.sdk.kotlin.services.ssm.model.ParameterType
import aws.sdk.kotlin.services.ssm.model.PutParameterRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class PutParameterSpec : FunSpec({
    test("execute sends correct parameter details to SSM") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<SsmClient>()
        MockSsmClientBuildService.mockClient = client
        val service = project.gradle.sharedServices.registerIfAbsent("ssm", MockSsmClientBuildService::class)
        val requestSlot = slot<PutParameterRequest>()
        coEvery { client.putParameter(capture(requestSlot)) } returns mockk()

        val task = project.tasks.create("t", PutParameter::class.java)
        task.service.set(service)
        task.parameterName.set("/my/parameter")
        task.parameterValue.set("new-value")
        task.parameterType.set("SecureString")
        task.overwrite.set(true)

        task.execute()

        val captured = requestSlot.captured
        captured.name shouldBe "/my/parameter"
        captured.value shouldBe "new-value"
        captured.type shouldBe ParameterType.SecureString
        captured.overwrite shouldBe true
        MockSsmClientBuildService.mockClient = null
    }

    test("execute with optional overwrite omitted") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<SsmClient>()
        MockSsmClientBuildService.mockClient = client
        val service = project.gradle.sharedServices.registerIfAbsent("ssm2", MockSsmClientBuildService::class)
        val requestSlot = slot<PutParameterRequest>()
        coEvery { client.putParameter(capture(requestSlot)) } returns mockk()

        val task = project.tasks.create("t2", PutParameter::class.java)
        task.service.set(service)
        task.parameterName.set("/app/config")
        task.parameterValue.set("config-value")
        task.parameterType.set("String")

        task.execute()

        val captured = requestSlot.captured
        captured.name shouldBe "/app/config"
        captured.value shouldBe "config-value"
        captured.type shouldBe ParameterType.String
        captured.overwrite shouldBe null
        MockSsmClientBuildService.mockClient = null
    }

    test("execute verifies service client call is made") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<SsmClient>()
        MockSsmClientBuildService.mockClient = client
        val service = project.gradle.sharedServices.registerIfAbsent("ssm3", MockSsmClientBuildService::class)
        coEvery { client.putParameter(any()) } returns mockk()

        val task = project.tasks.create("t3", PutParameter::class.java)
        task.service.set(service)
        task.parameterName.set("/test/param")
        task.parameterValue.set("test-value")
        task.parameterType.set("String")

        task.execute()

        coVerify { client.putParameter(any()) }
        MockSsmClientBuildService.mockClient = null
    }
})
