package com.kelvsyc.gradle.aws.java.ses

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.BulkEmailDestinationStatus
import software.amazon.awssdk.services.ses.model.BulkEmailStatus
import software.amazon.awssdk.services.ses.model.SendBulkTemplatedEmailRequest
import software.amazon.awssdk.services.ses.model.SendBulkTemplatedEmailResponse

class AbstractSendBulkTemplatedMailSpec : FunSpec() {
    init {
        test("run - sends a single batch with all entries") {
            val ses = mockk<SesClient>()
            val requestSlot = slot<SendBulkTemplatedEmailRequest>()
            every { ses.sendBulkTemplatedEmail(capture(requestSlot)) } returns
                SendBulkTemplatedEmailResponse.builder()
                    .status(
                        BulkEmailDestinationStatus.builder().status(BulkEmailStatus.SUCCESS).build(),
                        BulkEmailDestinationStatus.builder().status(BulkEmailStatus.SUCCESS).build()
                    )
                    .build()

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractSendBulkTemplatedMail>("sendBulk") {
                client.set(ses)
                sender.set("sender@example.com")
                templateName.set("my-template")
                defaultTemplateData.set("{\"key\":\"default\"}")
                registerEntry("e1") { entry ->
                    entry.recipients.set(listOf("to1@example.com"))
                    entry.templateData.set("{\"key\":\"value1\"}")
                }
                registerEntry("e2") { entry ->
                    entry.recipients.set(listOf("to2@example.com"))
                    entry.ccAddresses.set(listOf("cc@example.com"))
                }
            }
            task.get().run()

            val captured = requestSlot.captured
            captured.source() shouldBe "sender@example.com"
            captured.template() shouldBe "my-template"
            captured.defaultTemplateData() shouldBe "{\"key\":\"default\"}"
            captured.destinations() shouldHaveSize 2

            val d1 = captured.destinations()[0]
            d1.destination().toAddresses() shouldBe listOf("to1@example.com")
            d1.replacementTemplateData() shouldBe "{\"key\":\"value1\"}"

            val d2 = captured.destinations()[1]
            d2.destination().toAddresses() shouldBe listOf("to2@example.com")
            d2.destination().ccAddresses() shouldBe listOf("cc@example.com")
        }

        test("run - chunks entries beyond SES batch size") {
            val ses = mockk<SesClient>()
            val requestSlots = mutableListOf<SendBulkTemplatedEmailRequest>()
            every { ses.sendBulkTemplatedEmail(capture(requestSlots)) } returns
                SendBulkTemplatedEmailResponse.builder()
                    .status(
                        (1..50).map {
                            BulkEmailDestinationStatus.builder().status(BulkEmailStatus.SUCCESS).build()
                        }
                    )
                    .build()

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractSendBulkTemplatedMail>("sendBulk") {
                client.set(ses)
                sender.set("sender@example.com")
                templateName.set("my-template")
                repeat(120) { i ->
                    registerEntry("e$i") { entry ->
                        entry.recipients.set(listOf("to$i@example.com"))
                    }
                }
            }
            task.get().run()

            requestSlots shouldHaveSize 3
            requestSlots[0].destinations() shouldHaveSize 50
            requestSlots[1].destinations() shouldHaveSize 50
            requestSlots[2].destinations() shouldHaveSize 20
        }

        test("run - throws when batch reports per-entry failures") {
            val ses = mockk<SesClient>()
            every { ses.sendBulkTemplatedEmail(any<SendBulkTemplatedEmailRequest>()) } returns
                SendBulkTemplatedEmailResponse.builder()
                    .status(
                        BulkEmailDestinationStatus.builder().status(BulkEmailStatus.SUCCESS).build(),
                        BulkEmailDestinationStatus.builder()
                            .status(BulkEmailStatus.MESSAGE_REJECTED)
                            .error("Message rejected")
                            .build()
                    )
                    .build()

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractSendBulkTemplatedMail>("sendBulk") {
                client.set(ses)
                sender.set("sender@example.com")
                templateName.set("my-template")
                registerEntry("e1") { entry ->
                    entry.recipients.set(listOf("good@example.com"))
                }
                registerEntry("e2") { entry ->
                    entry.recipients.set(listOf("bad@example.com"))
                }
            }

            val ex = runCatching { task.get().run() }.exceptionOrNull()!!
            ex::class.java shouldBe IllegalStateException::class.java
            ex.message!! shouldContain "e2"
        }

        test("run - succeeds without defaultTemplateData") {
            val ses = mockk<SesClient>()
            val requestSlot = slot<SendBulkTemplatedEmailRequest>()
            every { ses.sendBulkTemplatedEmail(capture(requestSlot)) } returns
                SendBulkTemplatedEmailResponse.builder()
                    .status(
                        BulkEmailDestinationStatus.builder().status(BulkEmailStatus.SUCCESS).build()
                    )
                    .build()

            val project = ProjectBuilder.builder().build()
            val task = project.tasks.register<AbstractSendBulkTemplatedMail>("sendBulk") {
                client.set(ses)
                sender.set("sender@example.com")
                templateName.set("my-template")
                registerEntry("e1") { entry ->
                    entry.recipients.set(listOf("to@example.com"))
                    entry.templateData.set("{\"key\":\"value\"}")
                }
            }
            task.get().run()

            requestSlot.captured.defaultTemplateData() shouldBe null
        }
    }
}
