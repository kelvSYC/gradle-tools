package com.kelvsyc.gradle.google.cloud.pubsub

import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.pubsub.v1.ListTopicsRequest
import com.google.pubsub.v1.Topic
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

class ListTopicsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns topic resource names from paginated response") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(GoogleCloudPubSubBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockPubSubClientInfo::class, MockPubSubClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockPubSubClientInfo>("mock") {}

            val client = extension.getClient<TopicAdminClient, MockPubSubClientInfo>("mock").get()!!
            val topics = listOf(
                Topic.newBuilder().setName("projects/p/topics/alpha").build(),
                Topic.newBuilder().setName("projects/p/topics/bravo").build(),
            )
            val paged = mockk<TopicAdminClient.ListTopicsPagedResponse>()
            every { paged.iterateAll() } returns topics

            val slot = slot<ListTopicsRequest>()
            every { client.listTopics(capture(slot)) } returns paged

            val provider = project.providers.of(ListTopicsValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.projectId.set("p")
            }

            provider.get() shouldBe listOf("projects/p/topics/alpha", "projects/p/topics/bravo")
            slot.captured.project shouldBe "projects/p"
        }
    }
}
