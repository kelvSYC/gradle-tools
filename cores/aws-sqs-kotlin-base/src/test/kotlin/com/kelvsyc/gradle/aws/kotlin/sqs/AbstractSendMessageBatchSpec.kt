package com.kelvsyc.gradle.aws.kotlin.sqs

import aws.sdk.kotlin.services.sqs.SqsClient
import aws.sdk.kotlin.services.sqs.model.BatchResultErrorEntry
import aws.sdk.kotlin.services.sqs.model.MessageAttributeValue
import aws.sdk.kotlin.services.sqs.model.SendMessageBatchRequest
import aws.sdk.kotlin.services.sqs.model.SendMessageBatchResponse
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

class AbstractSendMessageBatchSpec : FunSpec() {
    init {
        test("run - sends a single batch with all entries") {
            val sqs = mockk<SqsClient>()
            val requestSlot = slot<SendMessageBatchRequest>()
            coEvery { sqs.sendMessageBatch(capture(requestSlot)) } returns SendMessageBatchResponse { failed = emptyList(); successful = emptyList() }

            val attribute = MessageAttributeValue { dataType = "String"; stringValue = "v" }

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractSendMessageBatch>("sendBatch") {
                client.set(sqs)
                queueUrl.set("https://sqs.us-east-1.amazonaws.com/123456789012/MyQueue")
                registerEntry("e1") {
                    messageBody.set("body 1")
                    attributes.put("attr", attribute)
                }
                registerEntry("e2") {
                    messageBody.set("body 2")
                }
            }
            task.get().run()

            val captured = requestSlot.captured
            captured.queueUrl shouldBe "https://sqs.us-east-1.amazonaws.com/123456789012/MyQueue"
            captured.entries!!.map { it.id }.shouldContainExactlyInAnyOrder("e1", "e2")
            val e1 = captured.entries!!.single { it.id == "e1" }
            e1.messageBody shouldBe "body 1"
            e1.messageAttributes!!["attr"]!!.stringValue shouldBe "v"
        }

        test("run - chunks entries beyond SQS batch size") {
            val sqs = mockk<SqsClient>()
            val requestSlots = mutableListOf<SendMessageBatchRequest>()
            coEvery { sqs.sendMessageBatch(capture(requestSlots)) } returns SendMessageBatchResponse { failed = emptyList(); successful = emptyList() }

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractSendMessageBatch>("sendBatch") {
                client.set(sqs)
                queueUrl.set("https://sqs.us-east-1.amazonaws.com/123456789012/MyQueue")
                repeat(25) { i ->
                    registerEntry("e$i") {
                        messageBody.set("body-$i")
                    }
                }
            }
            task.get().run()

            requestSlots shouldHaveSize 3
            requestSlots[0].entries!! shouldHaveSize 10
            requestSlots[1].entries!! shouldHaveSize 10
            requestSlots[2].entries!! shouldHaveSize 5
        }

        test("run - forwards FIFO ids per entry") {
            val sqs = mockk<SqsClient>()
            val requestSlot = slot<SendMessageBatchRequest>()
            coEvery { sqs.sendMessageBatch(capture(requestSlot)) } returns SendMessageBatchResponse { failed = emptyList(); successful = emptyList() }

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractSendMessageBatch>("sendBatch") {
                client.set(sqs)
                queueUrl.set("https://sqs.us-east-1.amazonaws.com/123456789012/MyQueue.fifo")
                registerEntry("e1") {
                    messageBody.set("body 1")
                    messageGroupId.set("group-a")
                    messageDeduplicationId.set("dedup-1")
                }
            }
            task.get().run()

            val entry = requestSlot.captured.entries!!.single()
            entry.messageGroupId shouldBe "group-a"
            entry.messageDeduplicationId shouldBe "dedup-1"
        }

        test("run - throws when batch reports per-entry failures") {
            val sqs = mockk<SqsClient>()
            coEvery { sqs.sendMessageBatch(any<SendMessageBatchRequest>()) } returns SendMessageBatchResponse {
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
            val task = project.tasks.register<AbstractSendMessageBatch>("sendBatch") {
                client.set(sqs)
                queueUrl.set("https://sqs.us-east-1.amazonaws.com/123456789012/MyQueue")
                registerEntry("e1") { messageBody.set("body 1") }
            }

            val ex = runCatching { task.get().run() }.exceptionOrNull()!!
            ex::class.java shouldBe GradleException::class.java
            ex.message!! shouldContain "e1"
        }
    }
}
