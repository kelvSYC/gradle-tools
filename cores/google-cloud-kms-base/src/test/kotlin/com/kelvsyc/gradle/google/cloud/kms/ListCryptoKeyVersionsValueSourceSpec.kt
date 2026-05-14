package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.CryptoKeyVersion
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.cloud.kms.v1.ListCryptoKeyVersionsRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListCryptoKeyVersionsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns crypto key version resource names from paginated response") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val versions = listOf(
                CryptoKeyVersion.newBuilder()
                    .setName("projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/my-key/cryptoKeyVersions/1")
                    .build(),
                CryptoKeyVersion.newBuilder()
                    .setName("projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/my-key/cryptoKeyVersions/2")
                    .build(),
            )
            val paged = mockk<KeyManagementServiceClient.ListCryptoKeyVersionsPagedResponse>()
            every { paged.iterateAll() } returns versions

            val slot = slot<ListCryptoKeyVersionsRequest>()
            every { client.listCryptoKeyVersions(capture(slot)) } returns paged

            val provider = project.providers.ofKt(ListCryptoKeyVersionsValueSource::class) {
                parameters.service.set(service)
                parameters.projectId.set("my-project")
                parameters.location.set("global")
                parameters.keyRingId.set("my-ring")
                parameters.cryptoKeyId.set("my-key")
            }

            provider.get() shouldBe listOf(
                "projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/my-key/cryptoKeyVersions/1",
                "projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/my-key/cryptoKeyVersions/2",
            )
            slot.captured.parent shouldBe
                "projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/my-key"
        }
    }
}
