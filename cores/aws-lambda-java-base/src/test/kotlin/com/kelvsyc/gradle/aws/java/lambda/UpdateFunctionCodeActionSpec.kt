package com.kelvsyc.gradle.aws.java.lambda

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.UpdateFunctionCodeRequest
import software.amazon.awssdk.services.lambda.model.UpdateFunctionCodeResponse

class UpdateFunctionCodeActionSpec : FunSpec() {
    init {
        test("execute - uploads zip file bytes to Lambda") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<LambdaClient>()
            MockLambdaClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("lambda", MockLambdaClientBuildService::class)
            val requestSlot = slot<UpdateFunctionCodeRequest>()
            every { client.updateFunctionCode(capture(requestSlot)) } returns mockk<UpdateFunctionCodeResponse>()

            val zipPath = project.layout.buildDirectory.file("fn.zip").get().asFile
            zipPath.parentFile.mkdirs()
            val expectedBytes = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
            zipPath.writeBytes(expectedBytes)

            val params = project.objects.newInstance<UpdateFunctionCodeAction.Parameters>()
            params.service.set(service)
            params.functionName.set("my-fn")
            params.zipFile.set(zipPath)
            params.publish.set(true)

            val action = object : UpdateFunctionCodeAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.functionName() shouldBe "my-fn"
            captured.publish() shouldBe true
            captured.zipFile().asByteArray().toList() shouldBe expectedBytes.toList()
        }
    }
}
