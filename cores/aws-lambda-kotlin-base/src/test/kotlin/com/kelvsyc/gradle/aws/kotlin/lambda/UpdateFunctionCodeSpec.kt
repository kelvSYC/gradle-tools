package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.model.UpdateFunctionCodeRequest
import aws.sdk.kotlin.services.lambda.model.UpdateFunctionCodeResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class UpdateFunctionCodeSpec : FunSpec({
    test("execute uploads zip file bytes to Lambda") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<LambdaClient>()
        MockLambdaClientBuildService.mockClient = client
        val service =
            project.gradle.sharedServices.registerIfAbsent("lambda", MockLambdaClientBuildService::class)
        val requestSlot = slot<UpdateFunctionCodeRequest>()
        coEvery { client.updateFunctionCode(capture(requestSlot)) } returns mockk<UpdateFunctionCodeResponse>()

        val zipPath = project.layout.buildDirectory.file("fn.zip").get().asFile
        zipPath.parentFile.mkdirs()
        val expectedBytes = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
        zipPath.writeBytes(expectedBytes)

        val task = project.tasks.create("t", UpdateFunctionCode::class.java)
        task.service.set(service)
        task.functionName.set("my-fn")
        task.zipFile.set(zipPath)
        task.publish.set(true)

        task.execute()

        val captured = requestSlot.captured
        captured.functionName shouldBe "my-fn"
        captured.publish shouldBe true
        captured.zipFile?.toList() shouldBe expectedBytes.toList()
        MockLambdaClientBuildService.mockClient = null
    }
})
