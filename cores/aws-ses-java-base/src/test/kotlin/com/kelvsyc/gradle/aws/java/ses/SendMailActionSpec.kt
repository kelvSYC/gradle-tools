package com.kelvsyc.gradle.aws.java.ses

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.SendEmailRequest
import software.amazon.awssdk.services.ses.model.SendEmailResponse

class SendMailActionSpec : FunSpec() {
    init {
        test("execute - passes correct sender and recipients to SES") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SesClient>()
            MockSesClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ses", MockSesClientBuildService::class)
            val requestSlot = slot<SendEmailRequest>()
            every { client.sendEmail(capture(requestSlot)) } returns mockk<SendEmailResponse>()

            val params = project.objects.newInstance<SendMailAction.Parameters>()
            params.service.set(service)
            params.sender.set("sender@example.com")
            params.recipients.set(listOf("to@example.com"))
            params.ccAddresses.set(listOf("cc@example.com"))
            params.bccAddresses.set(listOf("bcc@example.com"))
            params.subject.set("Hello")
            params.htmlMessage.set("<p>Hello</p>")
            params.textMessage.set("Hello")

            val action = object : SendMailAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.source() shouldBe "sender@example.com"
            captured.destination().toAddresses() shouldBe listOf("to@example.com")
            captured.destination().ccAddresses() shouldBe listOf("cc@example.com")
            captured.destination().bccAddresses() shouldBe listOf("bcc@example.com")
            captured.message().subject().data() shouldBe "Hello"
        }

        test("execute - includes html body when htmlMessage is present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SesClient>()
            MockSesClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ses", MockSesClientBuildService::class)
            val requestSlot = slot<SendEmailRequest>()
            every { client.sendEmail(capture(requestSlot)) } returns mockk<SendEmailResponse>()

            val params = project.objects.newInstance<SendMailAction.Parameters>()
            params.service.set(service)
            params.sender.set("sender@example.com")
            params.recipients.set(emptyList())
            params.ccAddresses.set(emptyList())
            params.bccAddresses.set(emptyList())
            params.subject.set("Hello")
            params.htmlMessage.set("<p>Hello</p>")

            val action = object : SendMailAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.message().body().html().data() shouldBe "<p>Hello</p>"
        }

        test("execute - includes text body when textMessage is present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SesClient>()
            MockSesClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ses", MockSesClientBuildService::class)
            val requestSlot = slot<SendEmailRequest>()
            every { client.sendEmail(capture(requestSlot)) } returns mockk<SendEmailResponse>()

            val params = project.objects.newInstance<SendMailAction.Parameters>()
            params.service.set(service)
            params.sender.set("sender@example.com")
            params.recipients.set(emptyList())
            params.ccAddresses.set(emptyList())
            params.bccAddresses.set(emptyList())
            params.subject.set("Hello")
            params.textMessage.set("Hello plain")

            val action = object : SendMailAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.message().body().text().data() shouldBe "Hello plain"
        }
    }
}
