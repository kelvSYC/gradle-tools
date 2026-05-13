package com.kelvsyc.gradle.aws.java.sqs

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse

class SendMessageActionSpec : FunSpec() {
    init {
        test("execute - passes correct queue url and message body to SQS") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SqsClient>()
            MockSqsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sqs", MockSqsClientBuildService::class)
            val requestSlot = slot<SendMessageRequest>()
            every { client.sendMessage(capture(requestSlot)) } returns mockk<SendMessageResponse>()

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(service)
            params.queueUrl.set("https://sqs.us-east-1.amazonaws.com/123456789012/MyQueue")
            params.messageBody.set("Hello, SQS!")

            val action = object : SendMessageAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.queueUrl() shouldBe "https://sqs.us-east-1.amazonaws.com/123456789012/MyQueue"
            captured.messageBody() shouldBe "Hello, SQS!"
        }

        test("execute - includes message attributes when present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SqsClient>()
            MockSqsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sqs", MockSqsClientBuildService::class)
            val requestSlot = slot<SendMessageRequest>()
            every { client.sendMessage(capture(requestSlot)) } returns mockk<SendMessageResponse>()

            val attributeValue = MessageAttributeValue.builder()
                .dataType("String")
                .stringValue("my-value")
                .build()

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(service)
            params.queueUrl.set("https://sqs.us-east-1.amazonaws.com/123456789012/MyQueue")
            params.messageBody.set("Hello")
            params.attributes.put("MyAttribute", attributeValue)

            val action = object : SendMessageAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.messageAttributes()["MyAttribute"]?.stringValue() shouldBe "my-value"
        }

        test("execute - omits message attributes when not present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SqsClient>()
            MockSqsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sqs", MockSqsClientBuildService::class)
            val requestSlot = slot<SendMessageRequest>()
            every { client.sendMessage(capture(requestSlot)) } returns mockk<SendMessageResponse>()

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(service)
            params.queueUrl.set("https://sqs.us-east-1.amazonaws.com/123456789012/MyQueue")
            params.messageBody.set("Hello")

            val action = object : SendMessageAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.messageAttributes().isEmpty() shouldBe true
        }

        test("execute - forwards FIFO message group id and deduplication id") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SqsClient>()
            MockSqsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sqs", MockSqsClientBuildService::class)
            val requestSlot = slot<SendMessageRequest>()
            every { client.sendMessage(capture(requestSlot)) } returns mockk<SendMessageResponse>()

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(service)
            params.queueUrl.set("https://sqs.us-east-1.amazonaws.com/123456789012/MyQueue.fifo")
            params.messageBody.set("Hello")
            params.messageGroupId.set("group-1")
            params.messageDeduplicationId.set("dedup-1")

            val action = object : SendMessageAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.messageGroupId() shouldBe "group-1"
            captured.messageDeduplicationId() shouldBe "dedup-1"
        }

        test("execute - omits FIFO ids when not present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SqsClient>()
            MockSqsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sqs", MockSqsClientBuildService::class)
            val requestSlot = slot<SendMessageRequest>()
            every { client.sendMessage(capture(requestSlot)) } returns mockk<SendMessageResponse>()

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(service)
            params.queueUrl.set("https://sqs.us-east-1.amazonaws.com/123456789012/MyQueue")
            params.messageBody.set("Hello")

            val action = object : SendMessageAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            (captured.messageGroupId() == null) shouldBe true
            (captured.messageDeduplicationId() == null) shouldBe true
        }
    }
}
