package com.kelvsyc.gradle.aws.kotlin.ses

import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.ses.model.SendRawEmailRequest
import aws.sdk.kotlin.services.ses.model.SendRawEmailResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class AbstractSendRawMailSpec : FunSpec({
    test("execute - passes correct request parameters to SES") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<SesClient>()
        MockSesClientBuildService.mockClient = client
        val service = project.gradle.sharedServices.registerIfAbsent("ses", MockSesClientBuildService::class)
        val requestSlot = slot<SendRawEmailRequest>()
        coEvery { client.sendRawEmail(capture(requestSlot)) } returns mockk<SendRawEmailResponse>()

        val rawBytes = "raw email content".toByteArray()

        val task = project.tasks.create("t", AbstractSendRawMail::class.java)
        task.service.set(service)
        task.sender.set("sender@example.com")
        task.message.set(rawBytes)

        task.execute()

        val captured = requestSlot.captured
        captured.source shouldBe "sender@example.com"
        captured.rawMessage!!.data!! shouldBe rawBytes
        MockSesClientBuildService.mockClient = null
    }
})
