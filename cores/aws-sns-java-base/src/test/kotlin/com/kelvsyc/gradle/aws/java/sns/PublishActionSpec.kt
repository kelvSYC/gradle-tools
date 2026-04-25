package com.kelvsyc.gradle.aws.java.sns

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.sns.MockSnsClientInfoInternal
import com.kelvsyc.gradle.plugins.SnsJavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse

class PublishActionSpec : FunSpec() {
    init {
        test("execute - passes correct topicArn and message to SNS") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SnsJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSnsClientInfo::class, MockSnsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSnsClientInfo>("mock") {}

            val client = extension.getClient<SnsClient, MockSnsClientInfo>("mock").get()!!
            val requestSlot = slot<PublishRequest>()
            every { client.publish(capture(requestSlot)) } returns mockk<PublishResponse>()

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
            captured.topicArn() shouldBe "arn:aws:sns:us-east-1:123456789012:MyTopic"
            captured.message() shouldBe "Hello, SNS!"
        }

        test("execute - includes subject when subject is present") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SnsJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSnsClientInfo::class, MockSnsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSnsClientInfo>("mock") {}

            val client = extension.getClient<SnsClient, MockSnsClientInfo>("mock").get()!!
            val requestSlot = slot<PublishRequest>()
            every { client.publish(capture(requestSlot)) } returns mockk<PublishResponse>()

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

            requestSlot.captured.subject() shouldBe "My Subject"
        }

        test("execute - excludes subject when subject is absent") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SnsJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSnsClientInfo::class, MockSnsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSnsClientInfo>("mock") {}

            val client = extension.getClient<SnsClient, MockSnsClientInfo>("mock").get()!!
            val requestSlot = slot<PublishRequest>()
            every { client.publish(capture(requestSlot)) } returns mockk<PublishResponse>()

            val params = project.objects.newInstance<PublishAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.topicArn.set("arn:aws:sns:us-east-1:123456789012:MyTopic")
            params.message.set("Hello, SNS!")

            val action = object : PublishAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.subject() shouldBe null
        }
    }
}
