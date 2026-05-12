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
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class SendMessageActionSpec : FunSpec() {
    init {
        test("execute - passes correct request parameters to SQS") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SqsClient>()
            MockSqsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sqs", MockSqsClientBuildService::class)
            val requestSlot = slot<SendMessageRequest>()
            coEvery { client.sendMessage(capture(requestSlot)) } returns mockk<SendMessageResponse>()

            val attribute = MessageAttributeValue { dataType = "String"; stringValue = "value" }

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(service)
            params.queueUrl.set("https://sqs.us-east-1.amazonaws.com/123456789012/test-queue")
            params.messageBody.set("Hello, SQS!")
            params.attributes.put("key", attribute)

            val action = object : SendMessageAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.queueUrl shouldBe "https://sqs.us-east-1.amazonaws.com/123456789012/test-queue"
            captured.messageBody shouldBe "Hello, SQS!"
            captured.messageAttributes!!["key"]!!.stringValue shouldBe "value"
        }

        test("execute - forwards FIFO message group id and deduplication id") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SqsClient>()
            MockSqsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sqs", MockSqsClientBuildService::class)
            val requestSlot = slot<SendMessageRequest>()
            coEvery { client.sendMessage(capture(requestSlot)) } returns mockk<SendMessageResponse>()

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(service)
            params.queueUrl.set("https://sqs.us-east-1.amazonaws.com/123456789012/test-queue.fifo")
            params.messageBody.set("Hello, FIFO!")
            params.messageGroupId.set("group-1")
            params.messageDeduplicationId.set("dedup-1")

            val action = object : SendMessageAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.messageGroupId shouldBe "group-1"
            captured.messageDeduplicationId shouldBe "dedup-1"
        }

        test("execute - sends message with empty attributes") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SqsClient>()
            MockSqsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sqs", MockSqsClientBuildService::class)
            val requestSlot = slot<SendMessageRequest>()
            coEvery { client.sendMessage(capture(requestSlot)) } returns mockk<SendMessageResponse>()

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(service)
            params.queueUrl.set("https://sqs.us-east-1.amazonaws.com/123456789012/test-queue")
            params.messageBody.set("Simple message")

            val action = object : SendMessageAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.queueUrl shouldBe "https://sqs.us-east-1.amazonaws.com/123456789012/test-queue"
            captured.messageBody shouldBe "Simple message"
            captured.messageAttributes shouldBe emptyMap<String, MessageAttributeValue>()
        }
    }
}
