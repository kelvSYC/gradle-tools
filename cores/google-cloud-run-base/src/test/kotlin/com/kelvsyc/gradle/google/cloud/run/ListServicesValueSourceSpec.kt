package com.kelvsyc.gradle.google.cloud.run

import com.google.cloud.run.v2.Service
import com.google.cloud.run.v2.ServicesClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListServicesValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns map of short name to HTTPS URI") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ServicesClient>()
            MockCloudRunServicesClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "services",
                MockCloudRunServicesClientBuildService::class,
            )

            val services = listOf(
                Service.newBuilder()
                    .setName("projects/p/locations/us-central1/services/alpha")
                    .setUri("https://alpha-abc123-us-central1.run.app")
                    .build(),
                Service.newBuilder()
                    .setName("projects/p/locations/us-central1/services/bravo")
                    .setUri("https://bravo-def456-us-central1.run.app")
                    .build(),
            )
            val paged = mockk<ServicesClient.ListServicesPagedResponse>()
            every { paged.iterateAll() } returns services

            val parentSlot = slot<String>()
            every { client.listServices(capture(parentSlot)) } returns paged

            val provider = project.providers.ofKt(ListServicesValueSource::class) {
                parameters.service.set(service)
                parameters.projectId.set("p")
                parameters.location.set("us-central1")
            }

            provider.get() shouldBe mapOf(
                "alpha" to "https://alpha-abc123-us-central1.run.app",
                "bravo" to "https://bravo-def456-us-central1.run.app",
            )
            parentSlot.captured shouldBe "projects/p/locations/us-central1"
        }

        test("obtain - returns empty map when no services exist") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ServicesClient>()
            MockCloudRunServicesClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "services-empty",
                MockCloudRunServicesClientBuildService::class,
            )

            val paged = mockk<ServicesClient.ListServicesPagedResponse>()
            every { paged.iterateAll() } returns emptyList()

            every { client.listServices(any<String>()) } returns paged

            val provider = project.providers.ofKt(ListServicesValueSource::class) {
                parameters.service.set(service)
                parameters.projectId.set("p")
                parameters.location.set("us-central1")
            }

            provider.get() shouldBe emptyMap()
        }
    }
}
