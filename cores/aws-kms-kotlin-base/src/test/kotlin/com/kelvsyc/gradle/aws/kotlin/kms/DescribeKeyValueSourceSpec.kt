package com.kelvsyc.gradle.aws.kotlin.kms

import aws.sdk.kotlin.services.kms.KmsClient
import aws.sdk.kotlin.services.kms.model.DescribeKeyRequest
import aws.sdk.kotlin.services.kms.model.DescribeKeyResponse
import aws.sdk.kotlin.services.kms.model.KeyMetadata
import aws.sdk.kotlin.services.kms.model.KmsException
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.kms.MockKmsClientInfoInternal
import com.kelvsyc.gradle.plugins.KmsKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class DescribeKeyValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns key ARN on success") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(KmsKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockKmsClientInfo::class, MockKmsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockKmsClientInfo>("mock") {}
            val slot = slot<DescribeKeyRequest>()
            val client = extension.getClient<KmsClient, MockKmsClientInfo>("mock").get()!!
            coEvery { client.describeKey(capture(slot)) } returns DescribeKeyResponse {
                keyMetadata = KeyMetadata {
                    keyId = "abcd1234-5678-90ab-cdef-EXAMPLE12345"
                    arn = "arn:aws:kms:us-east-1:123:key/abcd1234-5678-90ab-cdef-EXAMPLE12345"
                }
            }

            val provider = project.providers.of(DescribeKeyValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.keyId.set("alias/my-key")
            }
            val result = provider.get()

            result shouldBe "arn:aws:kms:us-east-1:123:key/abcd1234-5678-90ab-cdef-EXAMPLE12345"
            slot.captured.keyId shouldBe "alias/my-key"
        }

        test("obtain - returns null when KmsException is thrown") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(KmsKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockKmsClientInfo::class, MockKmsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockKmsClientInfo>("mock") {}
            val client = extension.getClient<KmsClient, MockKmsClientInfo>("mock").get()!!
            coEvery { client.describeKey(any<DescribeKeyRequest>()) } throws KmsException("not found")

            val provider = project.providers.of(DescribeKeyValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.keyId.set("alias/missing")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}
