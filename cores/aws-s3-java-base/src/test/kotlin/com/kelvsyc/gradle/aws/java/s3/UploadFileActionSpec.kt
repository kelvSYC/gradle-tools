package com.kelvsyc.gradle.aws.java.s3

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

class UploadFileActionSpec : FunSpec() {
    init {
        test("execute - passes correct request parameters") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<S3Client>()
            MockS3ClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("s3", MockS3ClientBuildService::class)
            val requestSlot = slot<PutObjectRequest>()
            every { client.putObject(capture(requestSlot), any<RequestBody>()) } returns mockk()

            val inputFile = tempfile()

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
            captured.bucket() shouldBe "my-bucket"
            captured.key() shouldBe "my/key"
        }
    }
}
