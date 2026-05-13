package com.kelvsyc.gradle.aws.kotlin.ses

import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.ses.model.SendEmailRequest
import aws.sdk.kotlin.services.ses.model.SendEmailResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class SendMailActionSpec : FunSpec() {
    init {
        test("execute - passes correct request parameters to SES") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SesClient>()
            MockSesClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ses", MockSesClientBuildService::class)
            val requestSlot = slot<SendEmailRequest>()
            coEvery { client.sendEmail(capture(requestSlot)) } returns mockk<SendEmailResponse>()

            val params = project.objects.newInstance<SendMailAction.Parameters>()
            params.service.set(service)
            params.sender.set("sender@example.com")
            params.recipients.set(listOf("to@example.com"))
            params.ccAddresses.set(listOf("cc@example.com"))
            params.bccAddresses.set(listOf("bcc@example.com"))
            params.subject.set("Test Subject")
            params.htmlMessage.set("<p>Hello</p>")
            params.textMessage.set("Hello")

            val action = object : SendMailAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.source shouldBe "sender@example.com"
            captured.destination!!.toAddresses!! shouldContainAll listOf("to@example.com")
            captured.destination!!.ccAddresses!! shouldContainAll listOf("cc@example.com")
            captured.destination!!.bccAddresses!! shouldContainAll listOf("bcc@example.com")
            captured.message!!.subject!!.data shouldBe "Test Subject"
            captured.message!!.body!!.html!!.data shouldBe "<p>Hello</p>"
            captured.message!!.body!!.text!!.data shouldBe "Hello"
        }
    }
}
