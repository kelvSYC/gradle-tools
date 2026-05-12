package com.kelvsyc.gradle.aws.java.kms

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.kms.KmsClient
import software.amazon.awssdk.services.kms.model.KeyListEntry
import software.amazon.awssdk.services.kms.model.ListKeysRequest
import software.amazon.awssdk.services.kms.model.ListKeysResponse
import software.amazon.awssdk.services.kms.paginators.ListKeysIterable
import java.util.stream.Stream

class ListKeysValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns map of key IDs to ARNs") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KmsClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("kms", MockKmsClientBuildService::class)

            val key1 = mockk<KeyListEntry>()
            every { key1.keyId() } returns "key-1"
            every { key1.keyArn() } returns "arn:aws:kms:us-east-1:123:key/key-1"

            val key2 = mockk<KeyListEntry>()
            every { key2.keyId() } returns "key-2"
            every { key2.keyArn() } returns "arn:aws:kms:us-east-1:123:key/key-2"

            val response = mockk<ListKeysResponse>()
            every { response.keys() } returns listOf(key1, key2)

            val paginator = mockk<ListKeysIterable>()
            every { paginator.stream() } returns Stream.of(response)

            every { client.listKeysPaginator(any<ListKeysRequest>()) } returns paginator

            val provider = project.providers.ofKt(ListKeysValueSource::class) {
                parameters.service.set(service)
            }
            val result = provider.get()

            result shouldHaveSize 2
            result shouldContain ("key-1" to "arn:aws:kms:us-east-1:123:key/key-1")
            result shouldContain ("key-2" to "arn:aws:kms:us-east-1:123:key/key-2")
        }
    }
}
