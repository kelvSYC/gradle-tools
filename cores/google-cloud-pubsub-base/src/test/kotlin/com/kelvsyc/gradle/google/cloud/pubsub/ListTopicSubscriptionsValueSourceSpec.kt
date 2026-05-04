package com.kelvsyc.gradle.google.cloud.pubsub

import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.pubsub.v1.ListTopicSubscriptionsRequest
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.google.cloud.pubsub.MockPubSubClientInfoInternal
import com.kelvsyc.gradle.plugins.GoogleCloudPubSubBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class ListTopicSubscriptionsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns subscription names for the given topic") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(GoogleCloudPubSubBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockPubSubClientInfo::class, MockPubSubClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockPubSubClientInfo>("mock") {}

            val client = extension.getClient<TopicAdminClient, MockPubSubClientInfo>("mock").get()!!
            val subscriptions = listOf(
                "projects/p/subscriptions/sub-a",
                "projects/p/subscriptions/sub-b",
            )
            val paged = mockk<TopicAdminClient.ListTopicSubscriptionsPagedResponse>()
            every { paged.iterateAll() } returns subscriptions

            val slot = slot<ListTopicSubscriptionsRequest>()
            every { client.listTopicSubscriptions(capture(slot)) } returns paged

            val provider = project.providers.of(ListTopicSubscriptionsValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
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
