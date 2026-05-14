package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.cloud.kms.v1.KeyRing
import com.google.cloud.kms.v1.ListKeyRingsRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListKeyRingsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns key ring resource names from paginated response") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val rings = listOf(
                KeyRing.newBuilder().setName("projects/my-project/locations/global/keyRings/ring-a").build(),
                KeyRing.newBuilder().setName("projects/my-project/locations/global/keyRings/ring-b").build(),
            )
            val paged = mockk<KeyManagementServiceClient.ListKeyRingsPagedResponse>()
            every { paged.iterateAll() } returns rings

            val slot = slot<ListKeyRingsRequest>()
            every { client.listKeyRings(capture(slot)) } returns paged

            val provider = project.providers.ofKt(ListKeyRingsValueSource::class) {
                parameters.service.set(service)
                parameters.projectId.set("my-project")
                parameters.location.set("global")
            }

            provider.get() shouldBe listOf(
                "projects/my-project/locations/global/keyRings/ring-a",
                "projects/my-project/locations/global/keyRings/ring-b",
            )
            slot.captured.parent shouldBe "projects/my-project/locations/global"
        }
    }
}
