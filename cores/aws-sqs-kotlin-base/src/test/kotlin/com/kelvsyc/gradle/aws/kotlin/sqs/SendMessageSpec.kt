package com.kelvsyc.gradle.aws.kotlin.sqs

import aws.sdk.kotlin.services.sqs.SqsClient
import aws.sdk.kotlin.services.sqs.model.MessageAttributeValue
import aws.sdk.kotlin.services.sqs.model.SendMessageRequest
import aws.sdk.kotlin.services.sqs.model.SendMessageResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class SendMessageSpec : FunSpec({
    test("execute passes correct request parameters to SQS") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<SqsClient>()
        MockSqsClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("sqs", MockSqsClientBuildService::class)
        val requestSlot = slot<SendMessageRequest>()
        coEvery { client.sendMessage(capture(requestSlot)) } returns mockk<SendMessageResponse>()

        val attribute = MessageAttributeValue { dataType = "String"; stringValue = "value" }

        val task = project.tasks.create("t", SendMessage::class.java)
        task.service.set(service)
        task.queueUrl.set("https://sqs.us-east-1.amazonaws.com/123456789012/test-queue")
        task.messageBody.set("Hello, SQS!")
        task.attributes.put("key", attribute)

        task.execute()

        val captured = requestSlot.captured
        captured.queueUrl shouldBe "https://sqs.us-east-1.amazonaws.com/123456789012/test-queue"
        captured.messageBody shouldBe "Hello, SQS!"
        captured.messageAttributes!!["key"]!!.stringValue shouldBe "value"
        MockSqsClientBuildService.mockClient = null
    }

    test("execute forwards FIFO message group id and deduplication id") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<SqsClient>()
        MockSqsClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("sqs2", MockSqsClientBuildService::class)
        val requestSlot = slot<SendMessageRequest>()
        coEvery { client.sendMessage(capture(requestSlot)) } returns mockk<SendMessageResponse>()

        val task = project.tasks.create("t2", SendMessage::class.java)
        task.service.set(service)
        task.queueUrl.set("https://sqs.us-east-1.amazonaws.com/123456789012/test-queue.fifo")
        task.messageBody.set("Hello, FIFO!")
        task.messageGroupId.set("group-1")
        task.messageDeduplicationId.set("dedup-1")

        task.execute()

        val captured = requestSlot.captured
        captured.messageGroupId shouldBe "group-1"
        captured.messageDeduplicationId shouldBe "dedup-1"
        MockSqsClientBuildService.mockClient = null
    }

    test("execute sends message with empty attributes") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<SqsClient>()
        MockSqsClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("sqs3", MockSqsClientBuildService::class)
        val requestSlot = slot<SendMessageRequest>()
        coEvery { client.sendMessage(capture(requestSlot)) } returns mockk<SendMessageResponse>()

        val task = project.tasks.create("t3", SendMessage::class.java)
        task.service.set(service)
        task.queueUrl.set("https://sqs.us-east-1.amazonaws.com/123456789012/test-queue")
        task.messageBody.set("Simple message")

        task.execute()

        val captured = requestSlot.captured
        captured.queueUrl shouldBe "https://sqs.us-east-1.amazonaws.com/123456789012/test-queue"
        captured.messageBody shouldBe "Simple message"
        captured.messageAttributes shouldBe emptyMap<String, MessageAttributeValue>()
        MockSqsClientBuildService.mockClient = null
    }
})
