package com.kelvsyc.gradle.google.cloud.pubsub

import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.pubsub.v1.PublishResponse
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.TopicName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

class AbstractPublishBatchSpec : FunSpec() {
    init {
        test("run - publishes all entries in a single request when under batch limit") {
            val topicAdmin = mockk<TopicAdminClient>()
            val messagesSlot = slot<List<PubsubMessage>>()
            every { topicAdmin.publish(any<TopicName>(), capture(messagesSlot)) } returns
                PublishResponse.newBuilder().addMessageIds("1").addMessageIds("2").build()

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractPublishBatch>("publishBatch") {
                client.set(topicAdmin)
                projectId.set("my-project")
                topicId.set("my-topic")
                registerEntry("e1") { data.set("msg 1") }
                registerEntry("e2") {
                    data.set("msg 2")
                    attributes.put("k", "v")
                }
            }
            task.get().run()

            messagesSlot.captured shouldHaveSize 2
            messagesSlot.captured[0].data.toStringUtf8() shouldBe "msg 1"
            messagesSlot.captured[1].data.toStringUtf8() shouldBe "msg 2"
            messagesSlot.captured[1].attributesMap shouldBe mapOf("k" to "v")
        }

        test("run - forwards ordering key per entry") {
            val topicAdmin = mockk<TopicAdminClient>()
            val messagesSlot = slot<List<PubsubMessage>>()
            every { topicAdmin.publish(any<TopicName>(), capture(messagesSlot)) } returns
                PublishResponse.newBuilder().addMessageIds("1").build()

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractPublishBatch>("publishBatch") {
                client.set(topicAdmin)
                projectId.set("my-project")
                topicId.set("my-topic")
                registerEntry("e1") {
                    data.set("ordered")
                    orderingKey.set("key-a")
                }
            }
            task.get().run()

            messagesSlot.captured.single().orderingKey shouldBe "key-a"
        }
    }
}
