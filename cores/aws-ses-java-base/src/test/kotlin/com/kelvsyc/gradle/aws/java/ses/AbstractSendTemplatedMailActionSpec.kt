package com.kelvsyc.gradle.aws.java.ses

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.ses.MockSesClientInfoInternal
import com.kelvsyc.gradle.plugins.SesJavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.SendTemplatedEmailRequest
import software.amazon.awssdk.services.ses.model.SendTemplatedEmailResponse

class AbstractSendTemplatedMailActionSpec : FunSpec() {
    init {
        test("execute - passes correct parameters to SES") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SesJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSesClientInfo::class, MockSesClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSesClientInfo>("mock") {}

            val client = extension.getClient<SesClient, MockSesClientInfo>("mock").get()!!
            val requestSlot = slot<SendTemplatedEmailRequest>()
            every { client.sendTemplatedEmail(capture(requestSlot)) } returns mockk<SendTemplatedEmailResponse>()

            val params = project.objects.newInstance<AbstractSendTemplatedMailAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
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

