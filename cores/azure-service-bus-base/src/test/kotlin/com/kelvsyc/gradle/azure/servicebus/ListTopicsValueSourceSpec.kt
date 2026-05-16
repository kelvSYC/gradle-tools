package com.kelvsyc.gradle.azure.servicebus

import com.azure.core.http.rest.PagedIterable
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient
import com.azure.messaging.servicebus.administration.models.TopicProperties
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListTopicsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns topic names from paginated response") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ServiceBusAdministrationClient>()
            MockServiceBusAdministrationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("serviceBusAdmin", MockServiceBusAdministrationClientBuildService::class)

            val t1 = mockk<TopicProperties> { every { name } returns "topic-alpha" }
            val t2 = mockk<TopicProperties> { every { name } returns "topic-bravo" }
            val paged = mockk<PagedIterable<TopicProperties>>()
            every { paged.iterator() } returns mutableListOf(t1, t2).iterator()
            every { client.listTopics() } returns paged

            val provider = project.providers.ofKt(ListTopicsValueSource::class) {
                parameters.service.set(service)
            }

            provider.get() shouldBe listOf("topic-alpha", "topic-bravo")
        }
    }
}
