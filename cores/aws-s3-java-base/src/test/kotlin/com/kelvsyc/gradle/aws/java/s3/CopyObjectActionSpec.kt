package com.kelvsyc.gradle.aws.java.s3

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CopyObjectRequest

class CopyObjectActionSpec : FunSpec() {
    init {
        test("execute - passes correct request parameters") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<S3Client>()
            MockS3ClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("s3", MockS3ClientBuildService::class)
            val requestSlot = slot<CopyObjectRequest>()
            every { client.copyObject(capture(requestSlot)) } returns mockk()

            val params = project.objects.newInstance<CopyObjectAction.Parameters>()
            params.service.set(service)
            params.sourceBucket.set("src-bucket")
            params.sourceKey.set("src/key")
            params.destinationBucket.set("dst-bucket")
            params.destinationKey.set("dst/key")

            val action = object : CopyObjectAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.sourceBucket() shouldBe "src-bucket"
            captured.sourceKey() shouldBe "src/key"
            captured.destinationBucket() shouldBe "dst-bucket"
            captured.destinationKey() shouldBe "dst/key"
        }
    }
}
