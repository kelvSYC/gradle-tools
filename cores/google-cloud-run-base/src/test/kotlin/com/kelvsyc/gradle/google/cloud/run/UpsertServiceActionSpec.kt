package com.kelvsyc.gradle.google.cloud.run

import com.google.api.gax.longrunning.OperationFuture
import com.google.cloud.run.v2.Service
import com.google.cloud.run.v2.ServicesClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import com.google.api.gax.rpc.NotFoundException
import com.google.api.gax.rpc.StatusCode
import com.google.protobuf.FieldMask

class UpsertServiceActionSpec : FunSpec() {
    init {
        test("execute - creates service when it does not exist") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ServicesClient>()
            MockCloudRunServicesClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "services-create",
                MockCloudRunServicesClientBuildService::class,
            )

            val serviceName = "projects/p/locations/us-central1/services/my-service"
            val statusCode = mockk<StatusCode>()
            every { client.getService(serviceName) } throws NotFoundException(
                Exception("Service not found"),
                statusCode,
                false
            )

            val createSlot = slot<Service>()
            val parentSlot = slot<String>()
            val serviceIdSlot = slot<String>()
            val operationFuture = mockk<OperationFuture<Service, Service>>()
            every { operationFuture.get() } returns Service.newBuilder()
                .setName(serviceName)
                .build()
            every { client.createServiceAsync(capture(parentSlot), capture(createSlot), capture(serviceIdSlot)) } returns operationFuture

            val params = project.objects.newInstance<UpsertServiceAction.Parameters>()
            params.service.set(service)
            params.serviceName.set(serviceName)
            params.imageUri.set("gcr.io/p/my-image:v1")
            params.envVars.put("FOO", "bar")
            params.envVars.put("BAZ", "qux")

            val action = object : UpsertServiceAction() {
                override fun getParameters() = params
            }
            action.execute()

            parentSlot.captured shouldBe "projects/p/locations/us-central1"
            serviceIdSlot.captured shouldBe "my-service"
            createSlot.captured.template.containersList shouldHaveSize 1
            createSlot.captured.template.containersList[0].image shouldBe "gcr.io/p/my-image:v1"
            createSlot.captured.template.containersList[0].envList shouldHaveSize 2
        }

        test("execute - updates service when it exists") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ServicesClient>()
            MockCloudRunServicesClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "services-update",
                MockCloudRunServicesClientBuildService::class,
            )

            val serviceName = "projects/p/locations/us-central1/services/my-service"
            val existingService = Service.newBuilder()
                .setName(serviceName)
                .build()
            every { client.getService(serviceName) } returns existingService

            val updateSlot = slot<Service>()
            val maskSlot = slot<FieldMask>()
            val operationFuture = mockk<OperationFuture<Service, Service>>()
            every { operationFuture.get() } returns existingService
            every { client.updateServiceAsync(capture(updateSlot), capture(maskSlot)) } returns operationFuture

            val params = project.objects.newInstance<UpsertServiceAction.Parameters>()
            params.service.set(service)
            params.serviceName.set(serviceName)
            params.imageUri.set("gcr.io/p/my-image:v2")
            params.envVars.put("ENV", "production")

            val action = object : UpsertServiceAction() {
                override fun getParameters() = params
            }
            action.execute()

            updateSlot.captured.template.containersList shouldHaveSize 1
            updateSlot.captured.template.containersList[0].image shouldBe "gcr.io/p/my-image:v2"
            updateSlot.captured.template.containersList[0].envList shouldHaveSize 1
            updateSlot.captured.template.containersList[0].envList[0].name shouldBe "ENV"
            updateSlot.captured.template.containersList[0].envList[0].value shouldBe "production"
        }

        test("execute - update uses correct field mask") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ServicesClient>()
            MockCloudRunServicesClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "services-mask",
                MockCloudRunServicesClientBuildService::class,
            )

            val serviceName = "projects/p/locations/us-central1/services/my-service"
            val existingService = Service.newBuilder()
                .setName(serviceName)
                .build()
            every { client.getService(serviceName) } returns existingService

            val maskSlot = slot<FieldMask>()
            val operationFuture = mockk<OperationFuture<Service, Service>>()
            every { operationFuture.get() } returns existingService
            every { client.updateServiceAsync(any<Service>(), capture(maskSlot)) } returns operationFuture

            val params = project.objects.newInstance<UpsertServiceAction.Parameters>()
            params.service.set(service)
            params.serviceName.set(serviceName)
            params.imageUri.set("gcr.io/p/my-image:v3")

            val action = object : UpsertServiceAction() {
                override fun getParameters() = params
            }
            action.execute()

            maskSlot.captured.pathsList shouldBe listOf("template.containers")
        }
    }
}
