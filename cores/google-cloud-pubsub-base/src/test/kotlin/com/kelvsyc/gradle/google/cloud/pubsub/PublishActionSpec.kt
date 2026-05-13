package com.kelvsyc.gradle.google.cloud.pubsub

import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.pubsub.v1.PublishResponse
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.TopicName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class PublishActionSpec : FunSpec() {
    init {
        test("execute - publishes message with correct topic and data") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<TopicAdminClient>()
            MockTopicAdminClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("pubsub", MockTopicAdminClientBuildService::class)

            val topicSlot = slot<TopicName>()
            val messagesSlot = slot<List<PubsubMessage>>()
            every { client.publish(capture(topicSlot), capture(messagesSlot)) } returns
                PublishResponse.newBuilder().addMessageIds("1").build()

            val params = project.objects.newInstance<PublishAction.Parameters>()
            params.service.set(service)
            params.projectId.set("my-project")
            params.topicId.set("my-topic")
            params.data.set("Hello, Pub/Sub!")

            val action = object : PublishAction() {
                override fun getParameters() = params
            }
            action.execute()

            topicSlot.captured.toString() shouldBe "projects/my-project/topics/my-topic"
            messagesSlot.captured.single().data.toStringUtf8() shouldBe "Hello, Pub/Sub!"
        }

        test("execute - includes attributes when present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<TopicAdminClient>()
            MockTopicAdminClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("pubsub", MockTopicAdminClientBuildService::class)

            val messagesSlot = slot<List<PubsubMessage>>()
            every { client.publish(any<TopicName>(), capture(messagesSlot)) } returns
                PublishResponse.newBuilder().addMessageIds("1").build()

            val params = project.objects.newInstance<PublishAction.Parameters>()
            params.service.set(service)
            params.projectId.set("my-project")
            params.topicId.set("my-topic")
            params.data.set("msg")
            params.attributes.put("env", "prod")

            val action = object : PublishAction() {
                override fun getParameters() = params
            }
            action.execute()

            messagesSlot.captured.single().attributesMap shouldBe mapOf("env" to "prod")
        }

        test("execute - forwards ordering key when present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<TopicAdminClient>()
            MockTopicAdminClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("pubsub", MockTopicAdminClientBuildService::class)

            val messagesSlot = slot<List<PubsubMessage>>()
            every { client.publish(any<TopicName>(), capture(messagesSlot)) } returns
                PublishResponse.newBuilder().addMessageIds("1").build()

            val params = project.objects.newInstance<PublishAction.Parameters>()
            params.service.set(service)
            params.projectId.set("my-project")
            params.topicId.set("my-topic")
            params.data.set("ordered msg")
            params.orderingKey.set("key-1")

            val action = object : PublishAction() {
                override fun getParameters() = params
            }
            action.execute()

            messagesSlot.captured.single().orderingKey shouldBe "key-1"
        }
    }
}
