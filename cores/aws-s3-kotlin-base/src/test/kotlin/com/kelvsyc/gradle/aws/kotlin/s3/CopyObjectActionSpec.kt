package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.CopyObjectRequest
import aws.sdk.kotlin.services.s3.model.CopyObjectResponse
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

class CopyObjectActionSpec : FunSpec() {
    init {
        test("execute - URL-encodes source and forwards destination fields") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(S3KotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockS3ClientInfo::class, MockS3ClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockS3ClientInfo>("mock") {}

            val client = extension.getClient<S3Client, MockS3ClientInfo>("mock").get()!!
            val requestSlot = slot<CopyObjectRequest>()
            coEvery { client.copyObject(capture(requestSlot)) } returns mockk<CopyObjectResponse>()

            val params = project.objects.newInstance<CopyObjectAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.sourceBucket.set("src-bucket")
            params.sourceKey.set("path with spaces/key")
            params.destinationBucket.set("dst-bucket")
            params.destinationKey.set("dst/key")

            val action = object : CopyObjectAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.copySource shouldBe "src-bucket/path+with+spaces%2Fkey"
            captured.bucket shouldBe "dst-bucket"
            captured.key shouldBe "dst/key"
        }
    }
}
