package com.kelvsyc.gradle.aws.kotlin.sns

import aws.sdk.kotlin.services.sns.SnsClient
import aws.sdk.kotlin.services.sns.model.BatchResultErrorEntry
import aws.sdk.kotlin.services.sns.model.MessageAttributeValue
import aws.sdk.kotlin.services.sns.model.PublishBatchRequest
import aws.sdk.kotlin.services.sns.model.PublishBatchResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.api.GradleException
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

class AbstractPublishBatchSpec : FunSpec() {
    init {
        test("run - sends a single batch with all entries") {
            val sns = mockk<SnsClient>()
            val requestSlot = slot<PublishBatchRequest>()
            coEvery { sns.publishBatch(capture(requestSlot)) } returns PublishBatchResponse {
                successful = emptyList()
                failed = emptyList()
            }

            val attribute = MessageAttributeValue { dataType = "String"; stringValue = "v" }

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractPublishBatch>("publishBatch") {
                client.set(sns)
                topicArn.set("arn:aws:sns:us-east-1:123456789012:MyTopic")
                registerEntry("e1") { entry ->
                    entry.message.set("msg 1")
                    entry.subject.set("sub 1")
                    entry.attributes.put("attr", attribute)
                }
                registerEntry("e2") { entry ->
                    entry.message.set("msg 2")
                }
            }
            task.get().run()

            val captured = requestSlot.captured
            captured.topicArn shouldBe "arn:aws:sns:us-east-1:123456789012:MyTopic"
            captured.publishBatchRequestEntries!!.map { it.id }.shouldContainExactlyInAnyOrder("e1", "e2")
            val e1 = captured.publishBatchRequestEntries!!.single { it.id == "e1" }
            e1.message shouldBe "msg 1"
            e1.subject shouldBe "sub 1"
            e1.messageAttributes!!["attr"]!!.stringValue shouldBe "v"
        }

        test("run - chunks entries beyond SNS batch size") {
            val sns = mockk<SnsClient>()
            val requestSlots = mutableListOf<PublishBatchRequest>()
            coEvery { sns.publishBatch(capture(requestSlots)) } returns PublishBatchResponse {
                successful = emptyList()
                failed = emptyList()
            }

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractPublishBatch>("publishBatch") {
                client.set(sns)
                topicArn.set("arn:aws:sns:us-east-1:123456789012:MyTopic")
                repeat(25) { i ->
                    registerEntry("e$i") { entry ->
                        entry.message.set("msg-$i")
                    }
                }
            }
            task.get().run()

            requestSlots shouldHaveSize 3
            requestSlots[0].publishBatchRequestEntries!! shouldHaveSize 10
            requestSlots[1].publishBatchRequestEntries!! shouldHaveSize 10
            requestSlots[2].publishBatchRequestEntries!! shouldHaveSize 5
        }

        test("run - forwards FIFO ids per entry") {
            val sns = mockk<SnsClient>()
            val requestSlot = slot<PublishBatchRequest>()
            coEvery { sns.publishBatch(capture(requestSlot)) } returns PublishBatchResponse {
                successful = emptyList()
                failed = emptyList()
            }

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractPublishBatch>("publishBatch") {
                client.set(sns)
                topicArn.set("arn:aws:sns:us-east-1:123456789012:MyTopic.fifo")
                registerEntry("e1") { entry ->
                    entry.message.set("msg 1")
                    entry.messageGroupId.set("group-a")
                    entry.messageDeduplicationId.set("dedup-1")
                }
            }
            task.get().run()

            val entry = requestSlot.captured.publishBatchRequestEntries!!.single()
            entry.messageGroupId shouldBe "group-a"
            entry.messageDeduplicationId shouldBe "dedup-1"
        }

        test("run - throws when batch reports per-entry failures") {
            val sns = mockk<SnsClient>()
            coEvery { sns.publishBatch(any<PublishBatchRequest>()) } returns PublishBatchResponse {
                successful = emptyList()
                failed = listOf(
                    BatchResultErrorEntry {
                        id = "e1"
                        senderFault = true
                        code = "InvalidParameterValue"
                    }
                )
            }

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractPublishBatch>("publishBatch") {
                client.set(sns)
                topicArn.set("arn:aws:sns:us-east-1:123456789012:MyTopic")
                registerEntry("e1") { entry -> entry.message.set("msg 1") }
            }

            val ex = runCatching { task.get().run() }.exceptionOrNull()!!
            ex::class.java shouldBe GradleException::class.java
            ex.message!! shouldContain "e1"
        }
    }
}
