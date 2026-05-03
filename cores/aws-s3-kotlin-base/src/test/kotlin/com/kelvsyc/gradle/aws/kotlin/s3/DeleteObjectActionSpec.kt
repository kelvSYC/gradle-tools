package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import aws.sdk.kotlin.services.s3.model.DeleteObjectResponse
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.s3.MockS3ClientInfoInternal
import com.kelvsyc.gradle.plugins.S3KotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class DeleteObjectActionSpec : FunSpec() {
    init {
        test("execute - passes correct request parameters") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(S3KotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockS3ClientInfo::class, MockS3ClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockS3ClientInfo>("mock") {}

            val client = extension.getClient<S3Client, MockS3ClientInfo>("mock").get()!!
            val requestSlot = slot<DeleteObjectRequest>()
            coEvery { client.deleteObject(capture(requestSlot)) } returns mockk<DeleteObjectResponse>()

            val params = project.objects.newInstance<DeleteObjectAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.bucket.set("my-bucket")
            params.key.set("my/key")

            val action = object : DeleteObjectAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.bucket shouldBe "my-bucket"
            captured.key shouldBe "my/key"
        }
    }
}
