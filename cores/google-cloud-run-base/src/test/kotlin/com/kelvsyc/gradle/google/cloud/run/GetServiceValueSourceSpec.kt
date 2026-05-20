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

class GetServiceValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns HTTPS URI when service is deployed") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ServicesClient>()
            MockCloudRunServicesClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "services",
                MockCloudRunServicesClientBuildService::class,
            )

            val uri = "https://my-service-abc123-us-central1.run.app"
            val serviceProto = Service.newBuilder()
                .setName("projects/my-project/locations/us-central1/services/my-service")
                .setUri(uri)
                .build()

            val nameSlot = slot<String>()
            every { client.getService(capture(nameSlot)) } returns serviceProto

            val provider = project.providers.ofKt(GetServiceValueSource::class) {
                parameters.service.set(service)
                parameters.serviceName.set("projects/my-project/locations/us-central1/services/my-service")
            }

            provider.get() shouldBe uri
            nameSlot.captured shouldBe "projects/my-project/locations/us-central1/services/my-service"
        }


        test("obtain - returns null when service URI is blank") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ServicesClient>()
            MockCloudRunServicesClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "services-blank",
                MockCloudRunServicesClientBuildService::class,
            )

            val serviceProto = Service.newBuilder()
                .setName("projects/my-project/locations/us-central1/services/my-service")
                .setUri("")
                .build()

            every { client.getService(any<String>()) } returns serviceProto

            val provider = project.providers.ofKt(GetServiceValueSource::class) {
                parameters.service.set(service)
                parameters.serviceName.set("projects/my-project/locations/us-central1/services/my-service")
            }

            provider.orNull shouldBe null
        }
    }
}
