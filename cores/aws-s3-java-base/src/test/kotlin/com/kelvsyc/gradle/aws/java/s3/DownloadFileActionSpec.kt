package com.kelvsyc.gradle.aws.java.s3

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.s3.MockS3ClientInfoInternal
import com.kelvsyc.gradle.plugins.S3JavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import java.nio.file.Files

class DownloadFileActionSpec : FunSpec() {
    init {
        test("execute - passes correct request parameters") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(S3JavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockS3ClientInfo::class, MockS3ClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockS3ClientInfo>("mock") {}

            val client = extension.getClient<S3Client, MockS3ClientInfo>("mock").get()
            val requestSlot = slot<GetObjectRequest>()
            every {
                client.getObject(capture(requestSlot), any<ResponseTransformer<GetObjectResponse, *>>())
            } returns mockk()

            val outputFile = Files.createTempFile("download-test", ".bin")

            val params = project.objects.newInstance<DownloadFileAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.bucket.set("my-bucket")
            params.key.set("my/key")
            params.outputFile.set(outputFile.toFile())

            val action = object : DownloadFileAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.bucket() shouldBe "my-bucket"
            captured.key() shouldBe "my/key"

            outputFile.toFile().delete()
        }
    }
}
