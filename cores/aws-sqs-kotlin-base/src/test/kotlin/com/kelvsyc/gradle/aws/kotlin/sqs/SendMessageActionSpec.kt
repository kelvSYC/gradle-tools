package com.kelvsyc.gradle.aws.kotlin.sqs

import aws.sdk.kotlin.services.sqs.SqsClient
import aws.sdk.kotlin.services.sqs.model.MessageAttributeValue
import aws.sdk.kotlin.services.sqs.model.SendMessageRequest
import aws.sdk.kotlin.services.sqs.model.SendMessageResponse
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.sqs.MockSqsClientInfoInternal
import com.kelvsyc.gradle.plugins.SqsKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class SendMessageActionSpec : FunSpec() {
    init {
        test("execute - passes correct request parameters to SQS") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SqsKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSqsClientInfo::class, MockSqsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSqsClientInfo>("mock") {}

            val client = extension.getClient<SqsClient, MockSqsClientInfo>("mock").get()!!
            val requestSlot = slot<SendMessageRequest>()
            coEvery { client.sendMessage(capture(requestSlot)) } returns mockk<SendMessageResponse>()

            val attribute = MessageAttributeValue { dataType = "String"; stringValue = "value" }

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
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

        test("execute - sends message with empty attributes") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SqsKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSqsClientInfo::class, MockSqsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSqsClientInfo>("mock") {}

            val client = extension.getClient<SqsClient, MockSqsClientInfo>("mock").get()!!
            val requestSlot = slot<SendMessageRequest>()
            coEvery { client.sendMessage(capture(requestSlot)) } returns mockk<SendMessageResponse>()

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
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

