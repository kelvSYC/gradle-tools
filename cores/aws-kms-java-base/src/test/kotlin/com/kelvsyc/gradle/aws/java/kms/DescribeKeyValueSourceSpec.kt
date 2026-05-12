package com.kelvsyc.gradle.aws.java.kms

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
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
            val client = mockk<KmsClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("kms", MockKmsClientBuildService::class)
            val slot = slot<DescribeKeyRequest>()

            val metadata = mockk<KeyMetadata>()
            every { metadata.arn() } returns "arn:aws:kms:us-east-1:123:key/abc"
            val response = mockk<DescribeKeyResponse>()
            every { response.keyMetadata() } returns metadata
            every { client.describeKey(capture(slot)) } returns response

            val provider = project.providers.ofKt(DescribeKeyValueSource::class) {
                parameters.service.set(service)
                parameters.keyId.set("alias/my-key")
            }
            val result = provider.get()

            result shouldBe "arn:aws:kms:us-east-1:123:key/abc"
            slot.captured.keyId() shouldBe "alias/my-key"
        }

        test("obtain - returns null when KmsException is thrown") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KmsClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("kms", MockKmsClientBuildService::class)
            every {
                client.describeKey(any<DescribeKeyRequest>())
            } throws KmsException.builder().message("not found").build()

            val provider = project.providers.ofKt(DescribeKeyValueSource::class) {
                parameters.service.set(service)
                parameters.keyId.set("alias/missing")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}
