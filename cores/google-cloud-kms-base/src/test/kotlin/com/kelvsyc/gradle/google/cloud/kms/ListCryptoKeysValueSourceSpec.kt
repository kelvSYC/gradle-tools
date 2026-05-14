package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.CryptoKey
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.cloud.kms.v1.ListCryptoKeysRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListCryptoKeysValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns crypto key resource names from paginated response") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val keys = listOf(
                CryptoKey.newBuilder()
                    .setName("projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/key-1")
                    .build(),
                CryptoKey.newBuilder()
                    .setName("projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/key-2")
                    .build(),
            )
            val paged = mockk<KeyManagementServiceClient.ListCryptoKeysPagedResponse>()
            every { paged.iterateAll() } returns keys

            val slot = slot<ListCryptoKeysRequest>()
            every { client.listCryptoKeys(capture(slot)) } returns paged

            val provider = project.providers.ofKt(ListCryptoKeysValueSource::class) {
                parameters.service.set(service)
                parameters.projectId.set("my-project")
                parameters.location.set("global")
                parameters.keyRingId.set("my-ring")
            }

            provider.get() shouldBe listOf(
                "projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/key-1",
                "projects/my-project/locations/global/keyRings/my-ring/cryptoKeys/key-2",
            )
            slot.captured.parent shouldBe "projects/my-project/locations/global/keyRings/my-ring"
        }
    }
}
