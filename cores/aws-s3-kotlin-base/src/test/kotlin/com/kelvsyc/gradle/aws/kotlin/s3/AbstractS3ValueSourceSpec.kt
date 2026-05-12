package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class AbstractS3ValueSourceSpec : FunSpec() {
    abstract class MyS3ValueSource : AbstractS3ValueSource<MyS3ValueSource.Value, AbstractS3ValueSource.Parameters>() {
        object Value
        override fun doObtain(response: GetObjectResponse): Value = Value
    }

    init {
        test("get") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<S3Client>()
            MockS3ClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("s3", MockS3ClientBuildService::class)
            val slot = slot<GetObjectRequest>()
            coEvery { client.getObject<MyS3ValueSource.Value?>(capture(slot), any()) } coAnswers {
                val block = secondArg<suspend (GetObjectResponse) -> MyS3ValueSource.Value?>()
                block(mockk())
            }

            val provider = project.providers.ofKt(MyS3ValueSource::class) {
                parameters.service.set(service)
                parameters.bucket.set("bucket")
                parameters.key.set("key")
            }
            val result = provider.get()

            result shouldBeEqual MyS3ValueSource.Value
            slot.captured.bucket!! shouldBeEqual "bucket"
            slot.captured.key!! shouldBeEqual "key"
        }
    }
}
