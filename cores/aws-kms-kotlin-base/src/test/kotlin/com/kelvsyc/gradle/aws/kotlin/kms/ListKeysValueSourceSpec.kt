package com.kelvsyc.gradle.aws.kotlin.kms

import aws.sdk.kotlin.services.kms.KmsClient
import aws.sdk.kotlin.services.kms.model.KeyListEntry
import aws.sdk.kotlin.services.kms.model.ListKeysRequest
import aws.sdk.kotlin.services.kms.model.ListKeysResponse
import aws.sdk.kotlin.services.kms.paginators.listKeysPaginated
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.kms.MockKmsClientInfoInternal
import com.kelvsyc.gradle.plugins.KmsKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class ListKeysValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns map of key IDs to ARNs") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(KmsKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockKmsClientInfo::class, MockKmsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockKmsClientInfo>("mock") {}
            val client = extension.getClient<KmsClient, MockKmsClientInfo>("mock").get()!!

            mockkStatic("aws.sdk.kotlin.services.kms.paginators.PaginatorsKt")
            every { client.listKeysPaginated(any<ListKeysRequest>()) } returns flowOf(
                ListKeysResponse {
                    keys = listOf(
                        KeyListEntry { keyId = "key-1"; keyArn = "arn:aws:kms:us-east-1:123:key/key-1" },
                        KeyListEntry { keyId = "key-2"; keyArn = "arn:aws:kms:us-east-1:123:key/key-2" },
                    )
                }
            )

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
