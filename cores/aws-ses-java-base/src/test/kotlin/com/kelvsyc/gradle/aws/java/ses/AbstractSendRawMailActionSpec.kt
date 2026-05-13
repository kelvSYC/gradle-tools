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
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest
import software.amazon.awssdk.services.ses.model.SendRawEmailResponse

class AbstractSendRawMailActionSpec : FunSpec() {
    init {
        test("execute - passes correct sender and raw message to SES") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SesClient>()
            MockSesClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ses", MockSesClientBuildService::class)
            val requestSlot = slot<SendRawEmailRequest>()
            every { client.sendRawEmail(capture(requestSlot)) } returns mockk<SendRawEmailResponse>()

            val rawBytes = "raw-email-content".toByteArray()
            val params = project.objects.newInstance<AbstractSendRawMailAction.Parameters>()
            params.service.set(service)
            params.sender.set("sender@example.com")
            params.message.set(rawBytes)

            val action = object : AbstractSendRawMailAction<AbstractSendRawMailAction.Parameters>() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.source() shouldBe "sender@example.com"
            captured.rawMessage().data().asByteArray() shouldBe rawBytes
        }
    }
}
