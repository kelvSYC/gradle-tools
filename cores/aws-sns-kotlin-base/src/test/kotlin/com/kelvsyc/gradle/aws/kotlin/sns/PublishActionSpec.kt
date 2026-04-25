package com.kelvsyc.gradle.aws.kotlin.sns

import aws.sdk.kotlin.services.sns.SnsClient
import aws.sdk.kotlin.services.sns.model.PublishRequest
import aws.sdk.kotlin.services.sns.model.PublishResponse
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.sns.MockSnsClientInfoInternal
import com.kelvsyc.gradle.plugins.SnsKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class PublishActionSpec : FunSpec() {
    init {
        test("execute - passes correct topicArn and message to SNS") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SnsKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSnsClientInfo::class, MockSnsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSnsClientInfo>("mock") {}

            val client = extension.getClient<SnsClient, MockSnsClientInfo>("mock").get()!!
            val requestSlot = slot<PublishRequest>()
            coEvery { client.publish(capture(requestSlot)) } returns mockk<PublishResponse>()

            val params = project.objects.newInstance<PublishAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.topicArn.set("arn:aws:sns:us-east-1:123456789012:MyTopic")
            params.message.set("Hello, SNS!")

            val action = object : PublishAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.topicArn shouldBe "arn:aws:sns:us-east-1:123456789012:MyTopic"
            captured.message shouldBe "Hello, SNS!"
        }

        test("execute - includes subject when subject is present") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SnsKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSnsClientInfo::class, MockSnsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSnsClientInfo>("mock") {}

            val client = extension.getClient<SnsClient, MockSnsClientInfo>("mock").get()!!
            val requestSlot = slot<PublishRequest>()
            coEvery { client.publish(capture(requestSlot)) } returns mockk<PublishResponse>()

            val params = project.objects.newInstance<PublishAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.topicArn.set("arn:aws:sns:us-east-1:123456789012:MyTopic")
            params.subject.set("My Subject")
            params.message.set("Hello, SNS!")

            val action = object : PublishAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.subject shouldBe "My Subject"
        }

        test("execute - subject is null when subject is absent") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SnsKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSnsClientInfo::class, MockSnsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSnsClientInfo>("mock") {}

            val client = extension.getClient<SnsClient, MockSnsClientInfo>("mock").get()!!
            val requestSlot = slot<PublishRequest>()
            coEvery { client.publish(capture(requestSlot)) } returns mockk<PublishResponse>()

            val params = project.objects.newInstance<PublishAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.topicArn.set("arn:aws:sns:us-east-1:123456789012:MyTopic")
            params.message.set("Hello, SNS!")

            val action = object : PublishAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.subject shouldBe null
        }
    }
}
