package com.kelvsyc.gradle.aws.java.sns

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.BatchResultErrorEntry
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishBatchRequest
import software.amazon.awssdk.services.sns.model.PublishBatchResponse

class AbstractPublishBatchSpec : FunSpec() {
    init {
        test("run - sends a single batch with all entries") {
            val sns = mockk<SnsClient>()
            val requestSlot = slot<PublishBatchRequest>()
            every { sns.publishBatch(capture(requestSlot)) } returns
                PublishBatchResponse.builder().build()

            val attribute = MessageAttributeValue.builder().dataType("String").stringValue("v").build()

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractPublishBatch>("publishBatch") {
                client.set(sns)
                topicArn.set("arn:aws:sns:us-east-1:123456789012:MyTopic")
                registerEntry("e1") {
                    message.set("msg 1")
                    subject.set("sub 1")
                    attributes.put("attr", attribute)
                }
                registerEntry("e2") {
                    message.set("msg 2")
                }
            }
            task.get().run()

            val captured = requestSlot.captured
            captured.topicArn() shouldBe "arn:aws:sns:us-east-1:123456789012:MyTopic"
            captured.publishBatchRequestEntries().map { it.id() }.shouldContainExactlyInAnyOrder("e1", "e2")
            val e1 = captured.publishBatchRequestEntries().single { it.id() == "e1" }
            e1.message() shouldBe "msg 1"
            e1.subject() shouldBe "sub 1"
            e1.messageAttributes()["attr"]?.stringValue() shouldBe "v"
        }

        test("run - chunks entries beyond SNS batch size") {
            val sns = mockk<SnsClient>()
            val requestSlots = mutableListOf<PublishBatchRequest>()
            every { sns.publishBatch(capture(requestSlots)) } returns
                PublishBatchResponse.builder().build()

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractPublishBatch>("publishBatch") {
                client.set(sns)
                topicArn.set("arn:aws:sns:us-east-1:123456789012:MyTopic")
                repeat(25) { i ->
                    registerEntry("e$i") {
                        message.set("msg-$i")
                    }
                }
            }
            task.get().run()

            requestSlots shouldHaveSize 3
            requestSlots[0].publishBatchRequestEntries() shouldHaveSize 10
            requestSlots[1].publishBatchRequestEntries() shouldHaveSize 10
            requestSlots[2].publishBatchRequestEntries() shouldHaveSize 5
        }

        test("run - forwards FIFO ids per entry") {
            val sns = mockk<SnsClient>()
            val requestSlot = slot<PublishBatchRequest>()
            every { sns.publishBatch(capture(requestSlot)) } returns
                PublishBatchResponse.builder().build()

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractPublishBatch>("publishBatch") {
                client.set(sns)
                topicArn.set("arn:aws:sns:us-east-1:123456789012:MyTopic.fifo")
                registerEntry("e1") {
                    message.set("msg 1")
                    messageGroupId.set("group-a")
                    messageDeduplicationId.set("dedup-1")
                }
            }
            task.get().run()

            val entry = requestSlot.captured.publishBatchRequestEntries().single()
            entry.messageGroupId() shouldBe "group-a"
            entry.messageDeduplicationId() shouldBe "dedup-1"
        }

        test("run - throws when batch reports per-entry failures") {
            val sns = mockk<SnsClient>()
            every { sns.publishBatch(any<PublishBatchRequest>()) } returns
                PublishBatchResponse.builder()
                    .failed(
                        BatchResultErrorEntry.builder()
                            .id("e1")
                            .senderFault(true)
                            .code("InvalidParameterValue")
                            .build()
                    )
                    .build()

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractPublishBatch>("publishBatch") {
                client.set(sns)
                topicArn.set("arn:aws:sns:us-east-1:123456789012:MyTopic")
                registerEntry("e1") { message.set("msg 1") }
            }

            val ex = runCatching { task.get().run() }.exceptionOrNull()!!
            ex::class.java shouldBe IllegalStateException::class.java
            ex.message!! shouldContain "e1"
        }
    }
}
