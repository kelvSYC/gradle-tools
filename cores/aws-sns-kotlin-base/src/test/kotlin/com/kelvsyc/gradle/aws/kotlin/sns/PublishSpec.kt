package com.kelvsyc.gradle.aws.kotlin.sns

import aws.sdk.kotlin.services.sns.SnsClient
import aws.sdk.kotlin.services.sns.model.PublishRequest
import aws.sdk.kotlin.services.sns.model.PublishResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class PublishSpec : FunSpec({
    test("execute - passes correct topicArn and message to SNS") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<SnsClient>()
        MockSnsClientBuildService.mockClient = client
        val service = project.gradle.sharedServices.registerIfAbsent("sns", MockSnsClientBuildService::class)
        val requestSlot = slot<PublishRequest>()
        coEvery { client.publish(capture(requestSlot)) } returns mockk<PublishResponse>()

        val task = project.tasks.create("t", Publish::class.java)
        task.service.set(service)
        task.topicArn.set("arn:aws:sns:us-east-1:123456789012:MyTopic")
        task.message.set("Hello, SNS!")

        task.execute()

        val captured = requestSlot.captured
        captured.topicArn shouldBe "arn:aws:sns:us-east-1:123456789012:MyTopic"
        captured.message shouldBe "Hello, SNS!"
        MockSnsClientBuildService.mockClient = null
    }

    test("execute - includes subject when subject is present") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<SnsClient>()
        MockSnsClientBuildService.mockClient = client
        val service = project.gradle.sharedServices.registerIfAbsent("sns2", MockSnsClientBuildService::class)
        val requestSlot = slot<PublishRequest>()
        coEvery { client.publish(capture(requestSlot)) } returns mockk<PublishResponse>()

        val task = project.tasks.create("t2", Publish::class.java)
        task.service.set(service)
        task.topicArn.set("arn:aws:sns:us-east-1:123456789012:MyTopic")
        task.subject.set("My Subject")
        task.message.set("Hello, SNS!")

        task.execute()

        requestSlot.captured.subject shouldBe "My Subject"
        MockSnsClientBuildService.mockClient = null
    }

    test("execute - subject is null when subject is absent") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<SnsClient>()
        MockSnsClientBuildService.mockClient = client
        val service = project.gradle.sharedServices.registerIfAbsent("sns3", MockSnsClientBuildService::class)
        val requestSlot = slot<PublishRequest>()
        coEvery { client.publish(capture(requestSlot)) } returns mockk<PublishResponse>()

        val task = project.tasks.create("t3", Publish::class.java)
        task.service.set(service)
        task.topicArn.set("arn:aws:sns:us-east-1:123456789012:MyTopic")
        task.message.set("Hello, SNS!")

        task.execute()

        requestSlot.captured.subject shouldBe null
        MockSnsClientBuildService.mockClient = null
    }

    test("execute - forwards FIFO message group id and deduplication id") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<SnsClient>()
        MockSnsClientBuildService.mockClient = client
        val service = project.gradle.sharedServices.registerIfAbsent("sns4", MockSnsClientBuildService::class)
        val requestSlot = slot<PublishRequest>()
        coEvery { client.publish(capture(requestSlot)) } returns mockk<PublishResponse>()

        val task = project.tasks.create("t4", Publish::class.java)
        task.service.set(service)
        task.topicArn.set("arn:aws:sns:us-east-1:123456789012:MyTopic.fifo")
        task.message.set("Hello, FIFO!")
        task.messageGroupId.set("group-1")
        task.messageDeduplicationId.set("dedup-1")

        task.execute()

        val captured = requestSlot.captured
        captured.messageGroupId shouldBe "group-1"
        captured.messageDeduplicationId shouldBe "dedup-1"
        MockSnsClientBuildService.mockClient = null
    }
})
