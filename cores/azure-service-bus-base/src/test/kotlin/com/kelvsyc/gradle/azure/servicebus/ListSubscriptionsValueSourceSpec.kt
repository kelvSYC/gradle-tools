package com.kelvsyc.gradle.azure.servicebus

import com.azure.core.http.rest.PagedIterable
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient
import com.azure.messaging.servicebus.administration.models.SubscriptionProperties
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListSubscriptionsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns subscription names for the given topic") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ServiceBusAdministrationClient>()
            MockServiceBusAdministrationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("serviceBusAdmin", MockServiceBusAdministrationClientBuildService::class)

            val s1 = mockk<SubscriptionProperties> { every { subscriptionName } returns "sub-a" }
            val s2 = mockk<SubscriptionProperties> { every { subscriptionName } returns "sub-b" }
            val paged = mockk<PagedIterable<SubscriptionProperties>>()
            every { paged.iterator() } returns mutableListOf(s1, s2).iterator()

            val topicSlot = slot<String>()
            every { client.listSubscriptions(capture(topicSlot)) } returns paged

            val provider = project.providers.ofKt(ListSubscriptionsValueSource::class) {
                parameters.service.set(service)
                parameters.topicName.set("my-topic")
            }

            provider.get() shouldBe listOf("sub-a", "sub-b")
            topicSlot.captured shouldBe "my-topic"
        }
    }
}
