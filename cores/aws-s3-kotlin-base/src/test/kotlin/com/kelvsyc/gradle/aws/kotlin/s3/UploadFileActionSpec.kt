package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class UploadFileActionSpec : FunSpec() {
    init {
        test("execute - uploads file with correct request parameters") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<S3Client>()
            MockS3ClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("s3", MockS3ClientBuildService::class)
            val requestSlot = slot<PutObjectRequest>()
            coEvery { client.putObject(capture(requestSlot)) } returns mockk<PutObjectResponse>()

            val inputFile = tempfile()
            inputFile.writeText("upload-content")

            val params = project.objects.newInstance<UploadFileAction.Parameters>()
            params.service.set(service)
            params.bucket.set("my-bucket")
            params.key.set("my/key")
            params.inputFile.set(inputFile)

            val action = object : UploadFileAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.bucket shouldBe "my-bucket"
            captured.key shouldBe "my/key"
        }
    }
}
