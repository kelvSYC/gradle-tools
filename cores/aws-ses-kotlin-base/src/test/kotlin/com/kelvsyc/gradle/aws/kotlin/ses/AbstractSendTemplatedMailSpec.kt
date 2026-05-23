package com.kelvsyc.gradle.aws.kotlin.ses

import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.ses.model.SendTemplatedEmailRequest
import aws.sdk.kotlin.services.ses.model.SendTemplatedEmailResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class AbstractSendTemplatedMailSpec : FunSpec({
    test("execute - passes correct request parameters to SES") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<SesClient>()
        MockSesClientBuildService.mockClient = client
        val service = project.gradle.sharedServices.registerIfAbsent("ses", MockSesClientBuildService::class)
        val requestSlot = slot<SendTemplatedEmailRequest>()
        coEvery { client.sendTemplatedEmail(capture(requestSlot)) } returns mockk<SendTemplatedEmailResponse>()

        val task = project.tasks.create("t", AbstractSendTemplatedMail::class.java)
        task.service.set(service)
        task.sender.set("sender@example.com")
        task.recipients.set(listOf("to@example.com"))
        task.ccAddresses.set(listOf("cc@example.com"))
        task.bccAddresses.set(listOf("bcc@example.com"))
        task.templateName.set("my-template")
        task.templateJson.set("""{"key":"value"}""")

        task.execute()

        val captured = requestSlot.captured
        captured.source shouldBe "sender@example.com"
        captured.destination!!.toAddresses!! shouldContainAll listOf("to@example.com")
        captured.destination!!.ccAddresses!! shouldContainAll listOf("cc@example.com")
        captured.destination!!.bccAddresses!! shouldContainAll listOf("bcc@example.com")
        captured.template shouldBe "my-template"
        captured.templateData shouldBe """{"key":"value"}"""
        MockSesClientBuildService.mockClient = null
    }
})
