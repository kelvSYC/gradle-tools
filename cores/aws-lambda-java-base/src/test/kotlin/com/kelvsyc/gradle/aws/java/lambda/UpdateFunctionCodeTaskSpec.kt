package com.kelvsyc.gradle.aws.java.lambda

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.UpdateFunctionCodeRequest
import software.amazon.awssdk.services.lambda.model.UpdateFunctionCodeResponse

class UpdateFunctionCodeTaskSpec : FunSpec() {
    init {
        test("execute - uploads zip and publishes version") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<LambdaClient>()
            MockLambdaClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("lambda", MockLambdaClientBuildService::class)
            val requestSlot = slot<UpdateFunctionCodeRequest>()
            every { client.updateFunctionCode(capture(requestSlot)) } returns mockk<UpdateFunctionCodeResponse>()

            val zipFile = project.layout.buildDirectory.file("fn.zip").get().asFile
            zipFile.parentFile.mkdirs()
            val expectedBytes = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
            zipFile.writeBytes(expectedBytes)

            try {
                val task = project.tasks.register<UpdateFunctionCodeTask>("uploadFn") {
                    this.service.set(service)
                    this.functionName.set("my-fn")
                    this.zipFile.set(zipFile)
                    this.publish.set(true)
                }.get()

                task.execute()

                val captured = requestSlot.captured
                captured.functionName() shouldBe "my-fn"
                captured.publish() shouldBe true
                captured.zipFile().asByteArray().toList() shouldBe expectedBytes.toList()
            } finally {
                MockLambdaClientBuildService.mockClient = null
            }
        }

        test("execute - no publish flag - uploads without setting publish") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<LambdaClient>()
            MockLambdaClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("lambda", MockLambdaClientBuildService::class)
            val requestSlot = slot<UpdateFunctionCodeRequest>()
            every { client.updateFunctionCode(capture(requestSlot)) } returns mockk<UpdateFunctionCodeResponse>()

            val zipFile = project.layout.buildDirectory.file("fn.zip").get().asFile
            zipFile.parentFile.mkdirs()
            zipFile.writeBytes(byteArrayOf(0x50, 0x4B, 0x03, 0x04))

            try {
                val task = project.tasks.register<UpdateFunctionCodeTask>("uploadFn") {
                    this.service.set(service)
                    this.functionName.set("my-fn")
                    this.zipFile.set(zipFile)
                }.get()

                task.execute()

                requestSlot.captured.functionName() shouldBe "my-fn"
            } finally {
                MockLambdaClientBuildService.mockClient = null
            }
        }
    }
}
