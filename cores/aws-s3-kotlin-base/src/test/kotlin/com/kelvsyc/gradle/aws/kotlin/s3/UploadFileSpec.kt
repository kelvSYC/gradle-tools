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
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class UploadFileSpec : FunSpec() {
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

            val task = project.tasks.register<UploadFile>("uploadFile") {
                this.service.set(service)
                bucket.set("my-bucket")
                key.set("my/key")
                this.inputFile.set(inputFile)
            }.get()

            task.execute()

            val captured = requestSlot.captured
            captured.bucket shouldBe "my-bucket"
            captured.key shouldBe "my/key"
        }
    }
}
