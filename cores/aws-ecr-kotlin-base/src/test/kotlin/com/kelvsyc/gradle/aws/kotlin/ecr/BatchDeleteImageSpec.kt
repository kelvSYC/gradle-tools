package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.EcrClient
import aws.sdk.kotlin.services.ecr.model.BatchDeleteImageRequest
import aws.sdk.kotlin.services.ecr.model.BatchDeleteImageResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class BatchDeleteImageSpec : FunSpec() {
    init {
        test("execute - passes correct repositoryName and image tags") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<EcrClient>()
            MockEcrClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ecr", MockEcrClientBuildService::class)
            val requestSlot = slot<BatchDeleteImageRequest>()
            coEvery { client.batchDeleteImage(capture(requestSlot)) } returns mockk<BatchDeleteImageResponse>()

            val task = project.tasks.create("batchDeleteImage", BatchDeleteImage::class.java)
            task.service.set(service)
            task.repositoryName.set("my-repo")
            task.imageTags.set(setOf("v1.0", "v1.1"))

            task.execute()

            val captured = requestSlot.captured
            captured.repositoryName shouldBe "my-repo"
            captured.imageIds!!.mapNotNull { it.imageTag } shouldContainExactlyInAnyOrder listOf("v1.0", "v1.1")
        }
    }
}
