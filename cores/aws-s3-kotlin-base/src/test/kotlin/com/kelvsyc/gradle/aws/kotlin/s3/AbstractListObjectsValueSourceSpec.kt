package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.ListObjectsV2Request
import aws.sdk.kotlin.services.s3.model.ListObjectsV2Response
import aws.sdk.kotlin.services.s3.model.Object as S3Object
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.s3.MockS3ClientInfoInternal
import com.kelvsyc.gradle.plugins.S3KotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.mockk.coEvery
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class AbstractListObjectsValueSourceSpec : FunSpec() {
    abstract class KeysValueSource :
        AbstractListObjectsValueSource<List<String>, AbstractListObjectsValueSource.Parameters>() {
        override fun doObtain(objects: List<S3Object>): List<String> = objects.mapNotNull { it.key }
    }

    init {
        test("get - aggregates contents across pages and forwards request fields") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(S3KotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockS3ClientInfo::class, MockS3ClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockS3ClientInfo>("mock") {}

            val client = extension.getClient<S3Client, MockS3ClientInfo>("mock").get()!!
            val requestSlot = mutableListOf<ListObjectsV2Request>()

            val page1 = ListObjectsV2Response {
                contents = listOf(S3Object { key = "a" }, S3Object { key = "b" })
                isTruncated = true
                nextContinuationToken = "tok"
            }
            val page2 = ListObjectsV2Response {
                contents = listOf(S3Object { key = "c" })
                isTruncated = false
            }
            coEvery { client.listObjectsV2(capture(requestSlot)) } returnsMany listOf(page1, page2)

            val provider = project.providers.of(KeysValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.bucket.set("my-bucket")
                parameters.prefix.set("artifacts/")
            }
            val result = provider.get()

            result shouldContainExactly listOf("a", "b", "c")
            requestSlot[0].bucket!! shouldBeEqual "my-bucket"
            requestSlot[0].prefix!! shouldBeEqual "artifacts/"
            requestSlot[1].continuationToken!! shouldBeEqual "tok"
        }

        test("get - omits prefix when unset") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(S3KotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockS3ClientInfo::class, MockS3ClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockS3ClientInfo>("mock") {}

            val client = extension.getClient<S3Client, MockS3ClientInfo>("mock").get()!!
            val requestSlot = slot<ListObjectsV2Request>()

            coEvery { client.listObjectsV2(capture(requestSlot)) } returns ListObjectsV2Response {
                isTruncated = false
            }

            val provider = project.providers.of(KeysValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.bucket.set("my-bucket")
            }
            provider.get()

            requestSlot.captured.bucket!! shouldBeEqual "my-bucket"
            requestSlot.captured.prefix.shouldBeNull()
        }
    }
}
