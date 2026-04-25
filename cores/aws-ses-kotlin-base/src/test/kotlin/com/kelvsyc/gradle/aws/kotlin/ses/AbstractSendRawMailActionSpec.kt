package com.kelvsyc.gradle.aws.kotlin.ses

import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.ses.model.SendRawEmailRequest
import aws.sdk.kotlin.services.ses.model.SendRawEmailResponse
import io.mockk.mockk
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.ses.MockSesClientInfoInternal
import com.kelvsyc.gradle.plugins.SesKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class AbstractSendRawMailActionSpec : FunSpec() {
    abstract class ConcreteSendRawMailAction : AbstractSendRawMailAction<AbstractSendRawMailAction.Parameters>()

    init {
        test("execute - passes correct request parameters to SES") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SesKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSesClientInfo::class, MockSesClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSesClientInfo>("mock") {}

            val client = extension.getClient<SesClient, MockSesClientInfo>("mock").get()!!
            val requestSlot = slot<SendRawEmailRequest>()
            coEvery { client.sendRawEmail(capture(requestSlot)) } returns mockk<SendRawEmailResponse>()

            val rawBytes = "raw email content".toByteArray()

            val params = project.objects.newInstance<AbstractSendRawMailAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.sender.set("sender@example.com")
            params.message.set(rawBytes)

            val action = object : ConcreteSendRawMailAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.source shouldBe "sender@example.com"
            captured.rawMessage!!.data!! shouldBe rawBytes
        }
    }
}
