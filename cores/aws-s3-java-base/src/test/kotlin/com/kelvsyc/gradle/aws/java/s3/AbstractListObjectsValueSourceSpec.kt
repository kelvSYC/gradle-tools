package com.kelvsyc.gradle.aws.java.s3

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response
import software.amazon.awssdk.services.s3.model.S3Object
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable

class AbstractListObjectsValueSourceSpec : FunSpec() {
    abstract class KeysValueSource :
        AbstractListObjectsValueSource<List<String>, AbstractListObjectsValueSource.Parameters>() {
        override fun doObtain(objects: List<S3Object>): List<String> = objects.map { it.key() }
    }

    init {
        test("get - aggregates contents across pages and forwards request fields") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<S3Client>()
            MockS3ClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("s3", MockS3ClientBuildService::class)
            val requestSlot = slot<ListObjectsV2Request>()

            val page1 = ListObjectsV2Response.builder()
                .contents(S3Object.builder().key("a").build(), S3Object.builder().key("b").build())
                .build()
            val page2 = ListObjectsV2Response.builder()
                .contents(S3Object.builder().key("c").build())
                .build()
            val iterable = mockk<ListObjectsV2Iterable>()
            every { iterable.iterator() } answers { mutableListOf(page1, page2).iterator() }
            every { client.listObjectsV2Paginator(capture(requestSlot)) } returns iterable

            val provider = project.providers.ofKt(KeysValueSource::class) {
                parameters.service.set(service)
                parameters.bucket.set("my-bucket")
                parameters.prefix.set("artifacts/")
            }
            val result = provider.get()

            result shouldContainExactly listOf("a", "b", "c")
            requestSlot.captured.bucket() shouldBeEqual "my-bucket"
            requestSlot.captured.prefix() shouldBeEqual "artifacts/"
        }

        test("get - omits prefix when unset") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<S3Client>()
            MockS3ClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("s3", MockS3ClientBuildService::class)
            val requestSlot = slot<ListObjectsV2Request>()

            val iterable = mockk<ListObjectsV2Iterable>()
            every { iterable.iterator() } answers {
                mutableListOf(ListObjectsV2Response.builder().build()).iterator()
            }
            every { client.listObjectsV2Paginator(capture(requestSlot)) } returns iterable

            val provider = project.providers.ofKt(KeysValueSource::class) {
                parameters.service.set(service)
                parameters.bucket.set("my-bucket")
            }
            provider.get()

            requestSlot.captured.bucket() shouldBeEqual "my-bucket"
            (requestSlot.captured.prefix() == null) shouldBeEqual true
        }
    }
}
