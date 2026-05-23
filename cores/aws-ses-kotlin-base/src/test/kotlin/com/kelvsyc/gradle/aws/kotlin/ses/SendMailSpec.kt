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
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class SendMailSpec : FunSpec({
    test("execute - passes correct request parameters to SES") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<SesClient>()
        MockSesClientBuildService.mockClient = client
        val service = project.gradle.sharedServices.registerIfAbsent("ses", MockSesClientBuildService::class)
        val requestSlot = slot<SendEmailRequest>()
        coEvery { client.sendEmail(capture(requestSlot)) } returns mockk<SendEmailResponse>()

        val task = project.tasks.create("t", SendMail::class.java)
        task.service.set(service)
        task.sender.set("sender@example.com")
        task.recipients.set(listOf("to@example.com"))
        task.ccAddresses.set(listOf("cc@example.com"))
        task.bccAddresses.set(listOf("bcc@example.com"))
        task.subject.set("Test Subject")
        task.htmlMessage.set("<p>Hello</p>")
        task.textMessage.set("Hello")

        task.execute()

        val captured = requestSlot.captured
        captured.source shouldBe "sender@example.com"
        captured.destination!!.toAddresses!! shouldContainAll listOf("to@example.com")
        captured.destination!!.ccAddresses!! shouldContainAll listOf("cc@example.com")
        captured.destination!!.bccAddresses!! shouldContainAll listOf("bcc@example.com")
        captured.message!!.subject!!.data shouldBe "Test Subject"
        captured.message!!.body!!.html!!.data shouldBe "<p>Hello</p>"
        captured.message!!.body!!.text!!.data shouldBe "Hello"
        MockSesClientBuildService.mockClient = null
    }
})
