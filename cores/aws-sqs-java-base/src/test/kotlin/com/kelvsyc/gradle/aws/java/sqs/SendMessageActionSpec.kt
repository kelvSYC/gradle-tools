package com.kelvsyc.gradle.aws.java.sqs

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.sqs.MockSqsClientInfoInternal
import com.kelvsyc.gradle.plugins.SqsJavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse

class SendMessageActionSpec : FunSpec() {
    init {
        test("execute - passes correct queue url and message body to SQS") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SqsJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSqsClientInfo::class, MockSqsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSqsClientInfo>("mock") {}

            val client = extension.getClient<SqsClient, MockSqsClientInfo>("mock").get()!!
            val requestSlot = slot<SendMessageRequest>()
            every { client.sendMessage(capture(requestSlot)) } returns mockk<SendMessageResponse>()

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
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
            project.pluginManager.apply(SqsJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSqsClientInfo::class, MockSqsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSqsClientInfo>("mock") {}

            val client = extension.getClient<SqsClient, MockSqsClientInfo>("mock").get()!!
            val requestSlot = slot<SendMessageRequest>()
            every { client.sendMessage(capture(requestSlot)) } returns mockk<SendMessageResponse>()

            val attributeValue = MessageAttributeValue.builder()
                .dataType("String")
                .stringValue("my-value")
                .build()

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
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
            project.pluginManager.apply(SqsJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSqsClientInfo::class, MockSqsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSqsClientInfo>("mock") {}

            val client = extension.getClient<SqsClient, MockSqsClientInfo>("mock").get()!!
            val requestSlot = slot<SendMessageRequest>()
            every { client.sendMessage(capture(requestSlot)) } returns mockk<SendMessageResponse>()

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.queueUrl.set("https://sqs.us-east-1.amazonaws.com/123456789012/MyQueue")
            params.messageBody.set("Hello")

            val action = object : SendMessageAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.messageAttributes().isEmpty() shouldBe true
        }
    }
}

