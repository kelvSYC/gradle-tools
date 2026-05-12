package com.kelvsyc.gradle.aws.kotlin.kms

import aws.sdk.kotlin.services.kms.KmsClient
import aws.sdk.kotlin.services.kms.model.DescribeKeyRequest
import aws.sdk.kotlin.services.kms.model.DescribeKeyResponse
import aws.sdk.kotlin.services.kms.model.KeyMetadata
import aws.sdk.kotlin.services.kms.model.KmsException
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class DescribeKeyValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns key ARN on success") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KmsClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("kms", MockKmsClientBuildService::class)
            val slot = slot<DescribeKeyRequest>()
            coEvery { client.describeKey(capture(slot)) } returns DescribeKeyResponse {
                keyMetadata = KeyMetadata {
                    keyId = "abcd1234-5678-90ab-cdef-EXAMPLE12345"
                    arn = "arn:aws:kms:us-east-1:123:key/abcd1234-5678-90ab-cdef-EXAMPLE12345"
                }
            }

            val provider = project.providers.ofKt(DescribeKeyValueSource::class) {
                parameters.service.set(service)
                parameters.keyId.set("alias/my-key")
            }
            val result = provider.get()

            result shouldBe "arn:aws:kms:us-east-1:123:key/abcd1234-5678-90ab-cdef-EXAMPLE12345"
            slot.captured.keyId shouldBe "alias/my-key"
        }

        test("obtain - returns null when KmsException is thrown") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KmsClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("kms", MockKmsClientBuildService::class)
            coEvery { client.describeKey(any<DescribeKeyRequest>()) } throws KmsException("not found")

            val provider = project.providers.ofKt(DescribeKeyValueSource::class) {
                parameters.service.set(service)
                parameters.keyId.set("alias/missing")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}
