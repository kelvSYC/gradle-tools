package com.kelvsyc.gradle.google.cloud.pubsub

import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.pubsub.v1.ListTopicsRequest
import com.google.pubsub.v1.Topic
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListTopicsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns topic resource names from paginated response") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<TopicAdminClient>()
            MockTopicAdminClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("pubsub", MockTopicAdminClientBuildService::class)

            val topics = listOf(
                Topic.newBuilder().setName("projects/p/topics/alpha").build(),
                Topic.newBuilder().setName("projects/p/topics/bravo").build(),
            )
            val paged = mockk<TopicAdminClient.ListTopicsPagedResponse>()
            every { paged.iterateAll() } returns topics

            val slot = slot<ListTopicsRequest>()
            every { client.listTopics(capture(slot)) } returns paged

            val provider = project.providers.ofKt(ListTopicsValueSource::class) {
                parameters.service.set(service)
                parameters.projectId.set("p")
            }

            provider.get() shouldBe listOf("projects/p/topics/alpha", "projects/p/topics/bravo")
            slot.captured.project shouldBe "projects/p"
        }
    }
}
