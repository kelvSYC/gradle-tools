package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.EcrClient
import aws.sdk.kotlin.services.ecr.model.BatchDeleteImageRequest
import aws.sdk.kotlin.services.ecr.model.BatchDeleteImageResponse
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.ecr.MockEcrClientInfoInternal
import com.kelvsyc.gradle.plugins.EcrKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class BatchDeleteImageActionSpec : FunSpec() {
    init {
        test("execute - passes correct repositoryName and image tags") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(EcrKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockEcrClientInfo::class, MockEcrClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockEcrClientInfo>("mock") {}

            val client = extension.getClient<EcrClient, MockEcrClientInfo>("mock").get()!!
            val requestSlot = slot<BatchDeleteImageRequest>()
            coEvery { client.batchDeleteImage(capture(requestSlot)) } returns mockk<BatchDeleteImageResponse>()

            val params = project.objects.newInstance<BatchDeleteImageAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.repositoryName.set("my-repo")
            params.imageTags.set(setOf("v1.0", "v1.1"))

            val action = object : BatchDeleteImageAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.repositoryName shouldBe "my-repo"
            captured.imageIds!!.mapNotNull { it.imageTag } shouldContainExactlyInAnyOrder listOf("v1.0", "v1.1")
        }
    }
}
