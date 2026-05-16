package com.kelvsyc.gradle.azure.servicebus

import com.azure.messaging.servicebus.ServiceBusMessage
import com.azure.messaging.servicebus.ServiceBusSenderClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class SendMessageActionSpec : FunSpec() {
    init {
        test("execute - sends message with the specified body") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ServiceBusSenderClient>()
            MockServiceBusSenderClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("serviceBus", MockServiceBusSenderClientBuildService::class)

            val messageSlot = slot<ServiceBusMessage>()
            every { client.sendMessage(capture(messageSlot)) } just runs

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(service)
            params.body.set("Hello, Service Bus!")

            val action = object : SendMessageAction() {
                override fun getParameters() = params
            }
            action.execute()

            messageSlot.captured.body.toBytes() shouldBe "Hello, Service Bus!".toByteArray()
        }

        test("execute - includes subject when present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ServiceBusSenderClient>()
            MockServiceBusSenderClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("serviceBus", MockServiceBusSenderClientBuildService::class)

            val messageSlot = slot<ServiceBusMessage>()
            every { client.sendMessage(capture(messageSlot)) } just runs

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(service)
            params.body.set("msg")
            params.subject.set("Build Notification")

            val action = object : SendMessageAction() {
                override fun getParameters() = params
            }
            action.execute()

            messageSlot.captured.subject shouldBe "Build Notification"
        }

        test("execute - includes messageId when present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ServiceBusSenderClient>()
            MockServiceBusSenderClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("serviceBus", MockServiceBusSenderClientBuildService::class)

            val messageSlot = slot<ServiceBusMessage>()
            every { client.sendMessage(capture(messageSlot)) } just runs

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(service)
            params.body.set("msg")
            params.messageId.set("build-42")

            val action = object : SendMessageAction() {
                override fun getParameters() = params
            }
            action.execute()

            messageSlot.captured.messageId shouldBe "build-42"
        }

        test("execute - includes sessionId when present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ServiceBusSenderClient>()
            MockServiceBusSenderClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("serviceBus", MockServiceBusSenderClientBuildService::class)

            val messageSlot = slot<ServiceBusMessage>()
            every { client.sendMessage(capture(messageSlot)) } just runs

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(service)
            params.body.set("msg")
            params.sessionId.set("session-a")

            val action = object : SendMessageAction() {
                override fun getParameters() = params
            }
            action.execute()

            messageSlot.captured.sessionId shouldBe "session-a"
        }

        test("execute - includes partitionKey when present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ServiceBusSenderClient>()
            MockServiceBusSenderClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("serviceBus", MockServiceBusSenderClientBuildService::class)

            val messageSlot = slot<ServiceBusMessage>()
            every { client.sendMessage(capture(messageSlot)) } just runs

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(service)
            params.body.set("msg")
            params.partitionKey.set("partition-1")

            val action = object : SendMessageAction() {
                override fun getParameters() = params
            }
            action.execute()

            messageSlot.captured.partitionKey shouldBe "partition-1"
        }

        test("execute - includes applicationProperties when present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ServiceBusSenderClient>()
            MockServiceBusSenderClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("serviceBus", MockServiceBusSenderClientBuildService::class)

            val messageSlot = slot<ServiceBusMessage>()
            every { client.sendMessage(capture(messageSlot)) } just runs

            val params = project.objects.newInstance<SendMessageAction.Parameters>()
            params.service.set(service)
            params.body.set("msg")
            params.applicationProperties.put("env", "prod")

            val action = object : SendMessageAction() {
                override fun getParameters() = params
            }
            action.execute()

            messageSlot.captured.applicationProperties["env"] shouldBe "prod"
        }
    }
}
