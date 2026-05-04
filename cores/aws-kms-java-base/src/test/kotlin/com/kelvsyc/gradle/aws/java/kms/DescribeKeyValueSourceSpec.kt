package com.kelvsyc.gradle.aws.java.kms

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.kms.MockKmsClientInfoInternal
import com.kelvsyc.gradle.plugins.KmsJavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.kms.KmsClient
import software.amazon.awssdk.services.kms.model.DescribeKeyRequest
import software.amazon.awssdk.services.kms.model.DescribeKeyResponse
import software.amazon.awssdk.services.kms.model.KeyMetadata
import software.amazon.awssdk.services.kms.model.KmsException

class DescribeKeyValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns key ARN on success") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(KmsJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockKmsClientInfo::class, MockKmsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockKmsClientInfo>("mock") {}
            val slot = slot<DescribeKeyRequest>()
            val client = extension.getClient<KmsClient, _>("mock").get()

            val metadata = mockk<KeyMetadata>()
            every { metadata.arn() } returns "arn:aws:kms:us-east-1:123:key/abc"
            val response = mockk<DescribeKeyResponse>()
            every { response.keyMetadata() } returns metadata
            every { client.describeKey(capture(slot)) } returns response

            val provider = project.providers.of(DescribeKeyValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.keyId.set("alias/my-key")
            }
            val result = provider.get()

            result shouldBe "arn:aws:kms:us-east-1:123:key/abc"
            slot.captured.keyId() shouldBe "alias/my-key"
        }

        test("obtain - returns null when KmsException is thrown") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(KmsJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockKmsClientInfo::class, MockKmsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockKmsClientInfo>("mock") {}
            val client = extension.getClient<KmsClient, _>("mock").get()
            every { client.describeKey(any<DescribeKeyRequest>()) } throws KmsException.builder().message("not found").build()

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
