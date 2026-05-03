package com.kelvsyc.gradle.aws.java.s3

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.s3.MockS3ClientInfoInternal
import com.kelvsyc.gradle.plugins.S3JavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

class UploadFileActionSpec : FunSpec() {
    init {
        test("execute - passes correct request parameters") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(S3JavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockS3ClientInfo::class, MockS3ClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockS3ClientInfo>("mock") {}

            val client = extension.getClient<S3Client, MockS3ClientInfo>("mock").get()
            val requestSlot = slot<PutObjectRequest>()
            every { client.putObject(capture(requestSlot), any<RequestBody>()) } returns mockk()

            val inputFile = tempfile()

            val params = project.objects.newInstance<UploadFileAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
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
