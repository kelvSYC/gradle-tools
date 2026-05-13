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
import software.amazon.awssdk.services.ses.model.SendTemplatedEmailRequest
import software.amazon.awssdk.services.ses.model.SendTemplatedEmailResponse

class AbstractSendTemplatedMailActionSpec : FunSpec() {
    init {
        test("execute - passes correct parameters to SES") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SesClient>()
            MockSesClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ses", MockSesClientBuildService::class)
            val requestSlot = slot<SendTemplatedEmailRequest>()
            every { client.sendTemplatedEmail(capture(requestSlot)) } returns mockk<SendTemplatedEmailResponse>()

            val params = project.objects.newInstance<AbstractSendTemplatedMailAction.Parameters>()
            params.service.set(service)
            params.sender.set("sender@example.com")
            params.recipients.set(listOf("to@example.com"))
            params.ccAddresses.set(listOf("cc@example.com"))
            params.bccAddresses.set(listOf("bcc@example.com"))
            params.templateName.set("my-template")
            params.templateJson.set("{\"key\":\"value\"}")

            val action = object : AbstractSendTemplatedMailAction<AbstractSendTemplatedMailAction.Parameters>() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.source() shouldBe "sender@example.com"
            captured.destination().toAddresses() shouldBe listOf("to@example.com")
            captured.destination().ccAddresses() shouldBe listOf("cc@example.com")
            captured.destination().bccAddresses() shouldBe listOf("bcc@example.com")
            captured.template() shouldBe "my-template"
            captured.templateData() shouldBe "{\"key\":\"value\"}"
        }
    }
}
