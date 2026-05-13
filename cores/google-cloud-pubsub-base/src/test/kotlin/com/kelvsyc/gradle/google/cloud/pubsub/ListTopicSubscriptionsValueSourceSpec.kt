package com.kelvsyc.gradle.google.cloud.pubsub

import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.pubsub.v1.ListTopicSubscriptionsRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListTopicSubscriptionsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns subscription names for the given topic") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<TopicAdminClient>()
            MockTopicAdminClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("pubsub", MockTopicAdminClientBuildService::class)

            val subscriptions = listOf(
                "projects/p/subscriptions/sub-a",
                "projects/p/subscriptions/sub-b",
            )
            val paged = mockk<TopicAdminClient.ListTopicSubscriptionsPagedResponse>()
            every { paged.iterateAll() } returns subscriptions

            val slot = slot<ListTopicSubscriptionsRequest>()
            every { client.listTopicSubscriptions(capture(slot)) } returns paged

            val provider = project.providers.ofKt(ListTopicSubscriptionsValueSource::class) {
                parameters.service.set(service)
                parameters.projectId.set("p")
                parameters.topicId.set("my-topic")
            }

            provider.get() shouldBe listOf(
                "projects/p/subscriptions/sub-a",
                "projects/p/subscriptions/sub-b",
            )
            slot.captured.topic shouldBe "projects/p/topics/my-topic"
        }
    }
}
