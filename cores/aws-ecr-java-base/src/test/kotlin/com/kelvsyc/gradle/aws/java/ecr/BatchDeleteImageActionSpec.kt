package com.kelvsyc.gradle.aws.java.ecr

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.ecr.EcrClient
import software.amazon.awssdk.services.ecr.model.BatchDeleteImageRequest
import software.amazon.awssdk.services.ecr.model.BatchDeleteImageResponse

class BatchDeleteImageActionSpec : FunSpec() {
    init {
        test("execute - passes correct repositoryName and image tags") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<EcrClient>()
            MockEcrClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ecr", MockEcrClientBuildService::class)
            val requestSlot = slot<BatchDeleteImageRequest>()
            every { client.batchDeleteImage(capture(requestSlot)) } returns mockk<BatchDeleteImageResponse>()

            val params = project.objects.newInstance<BatchDeleteImageAction.Parameters>()
            params.service.set(service)
            params.repositoryName.set("my-repo")
            params.imageTags.set(setOf("v1.0", "v1.1"))

            val action = object : BatchDeleteImageAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.repositoryName() shouldBe "my-repo"
            captured.imageIds().mapNotNull { it.imageTag() } shouldContainExactlyInAnyOrder listOf("v1.0", "v1.1")
        }
    }
}
