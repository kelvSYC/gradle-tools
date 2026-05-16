package com.kelvsyc.gradle.azure.servicebus

import com.azure.core.http.rest.PagedIterable
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient
import com.azure.messaging.servicebus.administration.models.QueueProperties
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListQueuesValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns queue names from paginated response") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ServiceBusAdministrationClient>()
            MockServiceBusAdministrationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("serviceBusAdmin", MockServiceBusAdministrationClientBuildService::class)

            val q1 = mockk<QueueProperties> { every { name } returns "queue-alpha" }
            val q2 = mockk<QueueProperties> { every { name } returns "queue-bravo" }
            val paged = mockk<PagedIterable<QueueProperties>>()
            every { paged.iterator() } returns mutableListOf(q1, q2).iterator()
            every { client.listQueues() } returns paged

            val provider = project.providers.ofKt(ListQueuesValueSource::class) {
                parameters.service.set(service)
            }

            provider.get() shouldBe listOf("queue-alpha", "queue-bravo")
        }
    }
}
