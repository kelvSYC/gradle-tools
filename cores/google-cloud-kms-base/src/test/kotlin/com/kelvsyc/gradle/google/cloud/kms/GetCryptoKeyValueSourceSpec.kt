package com.kelvsyc.gradle.google.cloud.kms

import com.google.api.gax.rpc.ApiException
import com.google.cloud.kms.v1.CryptoKey
import com.google.cloud.kms.v1.KeyManagementServiceClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetCryptoKeyValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns canonical crypto key name on success") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val keyName = "projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/my-key"
            every { client.getCryptoKey(keyName) } returns
                CryptoKey.newBuilder().setName(keyName).build()

            val provider = project.providers.ofKt(GetCryptoKeyValueSource::class) {
                parameters.service.set(service)
                parameters.cryptoKeyName.set(keyName)
            }

            provider.get() shouldBe keyName
        }

        test("obtain - returns null when ApiException is thrown") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            every { client.getCryptoKey(any<String>()) } throws mockk<ApiException>(relaxed = true)

            val provider = project.providers.ofKt(GetCryptoKeyValueSource::class) {
                parameters.service.set(service)
                parameters.cryptoKeyName.set("projects/my-project/locations/global/keyRings/r/cryptoKeys/missing")
            }

            provider.orNull.shouldBeNull()
        }
    }
}
