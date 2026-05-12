package com.kelvsyc.gradle.aws.java.s3

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.core.ResponseBytes
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse

class AbstractS3ValueSourceSpec : FunSpec() {
    abstract class MyS3ValueSource :
        AbstractS3ValueSource<MyS3ValueSource.Result, AbstractS3ValueSource.Parameters>() {
        object Result
        override fun doObtain(content: ResponseBytes<GetObjectResponse>): Result? = Result
    }

    init {
        test("get") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<S3Client>()
            MockS3ClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("s3", MockS3ClientBuildService::class)
            val slot = slot<GetObjectRequest>()
            every { client.getObjectAsBytes(capture(slot)) } returns mockk()

            val provider = project.providers.ofKt(MyS3ValueSource::class) {
                parameters.service.set(service)
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
