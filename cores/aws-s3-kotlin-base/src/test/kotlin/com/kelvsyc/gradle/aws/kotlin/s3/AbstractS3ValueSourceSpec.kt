package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectResponse
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.s3.MockS3ClientInfoInternal
import com.kelvsyc.gradle.plugins.S3KotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class AbstractS3ValueSourceSpec : FunSpec() {
    abstract class MyS3ValueSource : AbstractS3ValueSource<MyS3ValueSource.Value, AbstractS3ValueSource.Parameters>() {
        object Value
        override fun doObtain(response: GetObjectResponse): Value = Value
    }

    init {
        test("get") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(S3KotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockS3ClientInfo::class, MockS3ClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockS3ClientInfo>("mock") {}
            val slot = slot<GetObjectRequest>()
            val client = extension.getClient<S3Client, MockS3ClientInfo>("mock").get()!!
            coEvery { client.getObject<MyS3ValueSource.Value?>(capture(slot), any()) } coAnswers {
                val block = secondArg<suspend (GetObjectResponse) -> MyS3ValueSource.Value?>()
                block(mockk())
            }

            val provider = project.providers.of(MyS3ValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
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
