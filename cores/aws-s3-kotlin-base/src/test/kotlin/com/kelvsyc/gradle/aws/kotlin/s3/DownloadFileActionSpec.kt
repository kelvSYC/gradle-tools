package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectResponse
import aws.smithy.kotlin.runtime.content.ByteStream
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Files

class DownloadFileActionSpec : FunSpec() {
    init {
        test("execute - downloads object to output file with correct request parameters") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<S3Client>()
            MockS3ClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("s3", MockS3ClientBuildService::class)
            val requestSlot = slot<GetObjectRequest>()
            val content = "downloaded-content"

            coEvery {
                client.getObject(capture(requestSlot), any<suspend (GetObjectResponse) -> Unit>())
            } coAnswers {
                val block = secondArg<suspend (GetObjectResponse) -> Unit>()
                val response = GetObjectResponse {
                    body = ByteStream.fromBytes(content.toByteArray())
                }
                block(response)
            }

            val outputFile = Files.createTempFile("download-test", ".bin")

            val params = project.objects.newInstance<DownloadFileAction.Parameters>()
            params.service.set(service)
            params.bucket.set("my-bucket")
            params.key.set("my/key")
            params.outputFile.set(outputFile.toFile())

            val action = object : DownloadFileAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.bucket shouldBe "my-bucket"
            captured.key shouldBe "my/key"
            outputFile.toFile().readText() shouldBe content

            outputFile.toFile().delete()
        }
    }
}
