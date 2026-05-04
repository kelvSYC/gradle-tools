package com.kelvsyc.gradle.aws.java.kms

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.kms.MockKmsClientInfoInternal
import com.kelvsyc.gradle.plugins.KmsJavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
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
            project.pluginManager.apply(KmsJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockKmsClientInfo::class, MockKmsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockKmsClientInfo>("mock") {}
            val client = extension.getClient<KmsClient, _>("mock").get()

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

            val provider = project.providers.of(ListKeysValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
            }
            val result = provider.get()

            result shouldHaveSize 2
            result shouldContain ("key-1" to "arn:aws:kms:us-east-1:123:key/key-1")
            result shouldContain ("key-2" to "arn:aws:kms:us-east-1:123:key/key-2")
        }
    }
}
