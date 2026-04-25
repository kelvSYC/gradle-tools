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
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest
import software.amazon.awssdk.services.ses.model.SendRawEmailResponse

class AbstractSendRawMailActionSpec : FunSpec() {
    init {
        test("execute - passes correct sender and raw message to SES") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SesJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSesClientInfo::class, MockSesClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSesClientInfo>("mock") {}

            val client = extension.getClient<SesClient, MockSesClientInfo>("mock").get()!!
            val requestSlot = slot<SendRawEmailRequest>()
            every { client.sendRawEmail(capture(requestSlot)) } returns mockk<SendRawEmailResponse>()

            val rawBytes = "raw-email-content".toByteArray()
            val params = project.objects.newInstance<AbstractSendRawMailAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
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

