package com.kelvsyc.gradle.aws.java.s3

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.s3.MockS3ClientInfoInternal
import com.kelvsyc.gradle.plugins.S3JavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.core.ResponseBytes
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse

class AbstractS3ValueSourceSpec : FunSpec() {
    abstract class MyS3ValueSource : AbstractS3ValueSource<MyS3ValueSource.Result, AbstractS3ValueSource.Parameters>() {
        object Result
        override fun doObtain(content: ResponseBytes<GetObjectResponse>): Result? = Result
    }

    init {
        test("get") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(S3JavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockS3ClientInfo::class, MockS3ClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockS3ClientInfo>("mock") {}
            val slot = slot<GetObjectRequest>()
            val client = extension.getClient<S3Client, _>("mock").get()
            every { client.getObjectAsBytes(capture(slot)) } returns mockk()

            val provider = project.providers.of(MyS3ValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.bucket.set("bucket")
                parameters.key.set("key")
            }
            val result = provider.get()

            result shouldBeEqual MyS3ValueSource.Result
            val capturedRequest = slot.captured
            capturedRequest.bucket() shouldBeEqual "bucket"
            capturedRequest.key() shouldBeEqual "key"
        }
    }
}
