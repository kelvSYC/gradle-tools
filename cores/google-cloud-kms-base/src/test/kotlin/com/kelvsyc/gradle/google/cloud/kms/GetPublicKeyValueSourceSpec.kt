package com.kelvsyc.gradle.google.cloud.kms

import com.google.api.gax.rpc.ApiException
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.cloud.kms.v1.PublicKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetPublicKeyValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns PEM public key on success") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val versionName =
                "projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/my-key/cryptoKeyVersions/1"
            val pem = "-----BEGIN PUBLIC KEY-----\nMFkw...\n-----END PUBLIC KEY-----\n"
            every { client.getPublicKey(versionName) } returns
                PublicKey.newBuilder().setPem(pem).build()

            val provider = project.providers.ofKt(GetPublicKeyValueSource::class) {
                parameters.service.set(service)
                parameters.cryptoKeyVersionName.set(versionName)
            }

            provider.get() shouldBe pem
        }

        test("obtain - returns null when ApiException is thrown") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            every { client.getPublicKey(any<String>()) } throws mockk<ApiException>(relaxed = true)

            val provider = project.providers.ofKt(GetPublicKeyValueSource::class) {
                parameters.service.set(service)
                parameters.cryptoKeyVersionName.set(
                    "projects/my-project/locations/global/keyRings/r/cryptoKeys/k/cryptoKeyVersions/1"
                )
            }

            provider.orNull.shouldBeNull()
        }
    }
}
