package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.CopyObjectRequest
import aws.sdk.kotlin.services.s3.model.CopyObjectResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class CopyObjectSpec : FunSpec() {
    init {
        test("execute - URL-encodes source and forwards destination fields") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<S3Client>()
            MockS3ClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("s3", MockS3ClientBuildService::class)
            val requestSlot = slot<CopyObjectRequest>()
            coEvery { client.copyObject(capture(requestSlot)) } returns mockk<CopyObjectResponse>()

            val task = project.tasks.register<CopyObject>("copyObject") {
                this.service.set(service)
                sourceBucket.set("src-bucket")
                sourceKey.set("path with spaces/key")
                destinationBucket.set("dst-bucket")
                destinationKey.set("dst/key")
            }.get()

            task.execute()

            val captured = requestSlot.captured
            captured.copySource shouldBe "src-bucket/path+with+spaces%2Fkey"
            captured.bucket shouldBe "dst-bucket"
            captured.key shouldBe "dst/key"
        }
    }
}
