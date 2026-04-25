package com.kelvsyc.gradle.aws.kotlin.ses

import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.ses.model.SendEmailRequest
import aws.sdk.kotlin.services.ses.model.SendEmailResponse
import io.mockk.mockk
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.ses.MockSesClientInfoInternal
import com.kelvsyc.gradle.plugins.SesKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class SendMailActionSpec : FunSpec() {
    init {
        test("execute - passes correct request parameters to SES") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SesKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSesClientInfo::class, MockSesClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSesClientInfo>("mock") {}

            val client = extension.getClient<SesClient, MockSesClientInfo>("mock").get()!!
            val requestSlot = slot<SendEmailRequest>()
            coEvery { client.sendEmail(capture(requestSlot)) } returns mockk<SendEmailResponse>()

            val params = project.objects.newInstance<SendMailAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
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
