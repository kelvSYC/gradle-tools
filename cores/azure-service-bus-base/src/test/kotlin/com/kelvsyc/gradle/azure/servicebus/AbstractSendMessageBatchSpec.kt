package com.kelvsyc.gradle.azure.servicebus

import com.azure.messaging.servicebus.ServiceBusMessage
import com.azure.messaging.servicebus.ServiceBusMessageBatch
import com.azure.messaging.servicebus.ServiceBusSenderClient
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

class AbstractSendMessageBatchSpec : FunSpec() {
    init {
        test("run - sends all entries as a single batch when they fit") {
            val sender = mockk<ServiceBusSenderClient>()
            val batch = mockk<ServiceBusMessageBatch>()
            val capturedMessages = mutableListOf<ServiceBusMessage>()
            every { sender.createMessageBatch() } returns batch
            every { batch.tryAddMessage(capture(capturedMessages)) } returns true
            every { batch.count } returns 2
            every { sender.sendMessages(batch) } just runs

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractSendMessageBatch>("sendBatch") {
                client.set(sender)
                registerEntry("e1") { it.body.set("msg 1") }
                registerEntry("e2") { it.body.set("msg 2") }
            }
            task.get().run()

            capturedMessages shouldHaveSize 2
            capturedMessages[0].body.toBytes() shouldBe "msg 1".toByteArray()
            capturedMessages[1].body.toBytes() shouldBe "msg 2".toByteArray()
            verify(exactly = 1) { sender.sendMessages(batch) }
        }

        test("run - flushes batch and starts a new one when tryAddMessage returns false") {
            val sender = mockk<ServiceBusSenderClient>()
            val batch1 = mockk<ServiceBusMessageBatch>()
            val batch2 = mockk<ServiceBusMessageBatch>()
            every { sender.createMessageBatch() } returnsMany listOf(batch1, batch2)
            every { batch1.tryAddMessage(any()) } returnsMany listOf(true, false)
            every { batch1.count } returns 1
            every { batch2.tryAddMessage(any()) } returns true
            every { batch2.count } returns 1
            every { sender.sendMessages(any<ServiceBusMessageBatch>()) } just runs

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractSendMessageBatch>("sendBatch") {
                client.set(sender)
                registerEntry("e1") { it.body.set("msg 1") }
                registerEntry("e2") { it.body.set("msg 2") }
            }
            task.get().run()

            verify(exactly = 1) { sender.sendMessages(batch1) }
            verify(exactly = 1) { sender.sendMessages(batch2) }
        }

        test("run - forwards subject and applicationProperties per entry") {
            val sender = mockk<ServiceBusSenderClient>()
            val batch = mockk<ServiceBusMessageBatch>()
            val capturedMessages = mutableListOf<ServiceBusMessage>()
            every { sender.createMessageBatch() } returns batch
            every { batch.tryAddMessage(capture(capturedMessages)) } returns true
            every { batch.count } returns 1
            every { sender.sendMessages(batch) } just runs

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractSendMessageBatch>("sendBatch") {
                client.set(sender)
                registerEntry("e1") { entry ->
                    entry.body.set("msg")
                    entry.subject.set("My Subject")
                    entry.applicationProperties.put("k", "v")
                }
            }
            task.get().run()

            capturedMessages.single().subject shouldBe "My Subject"
            capturedMessages.single().applicationProperties["k"] shouldBe "v"
        }

        test("run - throws when a single message exceeds maximum batch size") {
            val sender = mockk<ServiceBusSenderClient>()
            val batch = mockk<ServiceBusMessageBatch>()
            every { sender.createMessageBatch() } returns batch
            every { batch.tryAddMessage(any()) } returns false
            every { batch.count } returns 0
            every { sender.sendMessages(any<ServiceBusMessageBatch>()) } just runs

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractSendMessageBatch>("sendBatch") {
                client.set(sender)
                registerEntry("e1") { it.body.set("oversized") }
            }

            shouldThrow<IllegalStateException> { task.get().run() }
        }

        test("run - does nothing when entries is empty") {
            val sender = mockk<ServiceBusSenderClient>()
            val batch = mockk<ServiceBusMessageBatch>()
            every { sender.createMessageBatch() } returns batch
            every { batch.count } returns 0

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractSendMessageBatch>("sendBatch") {
                client.set(sender)
            }
            task.get().run()

            verify(exactly = 0) { sender.sendMessages(any<ServiceBusMessageBatch>()) }
        }
    }
}
