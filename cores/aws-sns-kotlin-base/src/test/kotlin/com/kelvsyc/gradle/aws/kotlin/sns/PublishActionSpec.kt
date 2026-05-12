package com.kelvsyc.gradle.aws.kotlin.sns

import aws.sdk.kotlin.services.sns.SnsClient
import aws.sdk.kotlin.services.sns.model.PublishRequest
import aws.sdk.kotlin.services.sns.model.PublishResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class PublishActionSpec : FunSpec() {
    init {
        test("execute - passes correct topicArn and message to SNS") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SnsClient>()
            MockSnsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sns", MockSnsClientBuildService::class)
            val requestSlot = slot<PublishRequest>()
            coEvery { client.publish(capture(requestSlot)) } returns mockk<PublishResponse>()

            val params = project.objects.newInstance<PublishAction.Parameters>()
            params.service.set(service)
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
            val client = mockk<SnsClient>()
            MockSnsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sns", MockSnsClientBuildService::class)
            val requestSlot = slot<PublishRequest>()
            coEvery { client.publish(capture(requestSlot)) } returns mockk<PublishResponse>()

            val params = project.objects.newInstance<PublishAction.Parameters>()
            params.service.set(service)
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
            val client = mockk<SnsClient>()
            MockSnsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sns", MockSnsClientBuildService::class)
            val requestSlot = slot<PublishRequest>()
            coEvery { client.publish(capture(requestSlot)) } returns mockk<PublishResponse>()

            val params = project.objects.newInstance<PublishAction.Parameters>()
            params.service.set(service)
            params.topicArn.set("arn:aws:sns:us-east-1:123456789012:MyTopic")
            params.message.set("Hello, SNS!")

            val action = object : PublishAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.subject shouldBe null
        }

        test("execute - forwards FIFO message group id and deduplication id") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SnsClient>()
            MockSnsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sns", MockSnsClientBuildService::class)
            val requestSlot = slot<PublishRequest>()
            coEvery { client.publish(capture(requestSlot)) } returns mockk<PublishResponse>()

            val params = project.objects.newInstance<PublishAction.Parameters>()
            params.service.set(service)
            params.topicArn.set("arn:aws:sns:us-east-1:123456789012:MyTopic.fifo")
            params.message.set("Hello, FIFO!")
            params.messageGroupId.set("group-1")
            params.messageDeduplicationId.set("dedup-1")

            val action = object : PublishAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.messageGroupId shouldBe "group-1"
            captured.messageDeduplicationId shouldBe "dedup-1"
        }
    }
}
