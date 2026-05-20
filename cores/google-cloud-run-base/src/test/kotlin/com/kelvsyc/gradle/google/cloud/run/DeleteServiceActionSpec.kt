package com.kelvsyc.gradle.google.cloud.run

import com.google.api.gax.longrunning.OperationFuture
import com.google.cloud.run.v2.Service
import com.google.cloud.run.v2.ServicesClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class DeleteServiceActionSpec : FunSpec() {
    init {
        test("execute - calls deleteServiceAsync with the correct service name") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ServicesClient>()
            MockCloudRunServicesClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "services-delete",
                MockCloudRunServicesClientBuildService::class,
            )

            val serviceName = "projects/p/locations/us-central1/services/my-service"
            val nameSlot = slot<String>()
            val operationFuture = mockk<OperationFuture<Service, Service>>()
            every { operationFuture.get() } returns Service.newBuilder()
                .setName(serviceName)
                .build()
            every { client.deleteServiceAsync(capture(nameSlot)) } returns operationFuture

            val params = project.objects.newInstance<DeleteServiceAction.Parameters>()
            params.service.set(service)
            params.serviceName.set(serviceName)

            val action = object : DeleteServiceAction() {
                override fun getParameters() = params
            }
            action.execute()

            nameSlot.captured shouldBe serviceName
        }
    }
}
