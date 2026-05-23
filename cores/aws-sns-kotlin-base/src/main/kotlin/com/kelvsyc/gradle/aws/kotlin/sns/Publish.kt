package com.kelvsyc.gradle.aws.kotlin.sns

import aws.sdk.kotlin.services.sns.model.PublishRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Task that publishes a single message to an SNS topic.
 *
 * Only simple messages, to be sent across all transport protocols, are supported. JSON messages are not supported.
 *
 * For FIFO topics, set [messageGroupId] (required) and optionally [messageDeduplicationId]
 * (required if content-based deduplication is not enabled on the topic).
 */
@UntrackedTask(because = "Communicates with AWS SNS; no local output")
abstract class Publish : DefaultTask() {

    /** The shared build service managing the SNS client. */
    @get:Internal
    abstract val service: Property<SnsClientBuildService>

    /** ARN of the target SNS topic. */
    @get:Input
    abstract val topicArn: Property<String>

    /** Optional subject for the message (used by SNS email transport). */
    @get:Optional
    @get:Input
    abstract val subject: Property<String>

    /** Body of the message. */
    @get:Input
    abstract val message: Property<String>

    /** Message group id; required for FIFO topics, must be unset for standard topics. */
    @get:Input
    @get:Optional
    abstract val messageGroupId: Property<String>

    /** Message deduplication id; used by FIFO topics without content-based deduplication. */
    @get:Input
    @get:Optional
    abstract val messageDeduplicationId: Property<String>

    @TaskAction
    fun execute() {
        val request = PublishRequest {
            topicArn = this@Publish.topicArn.get()
            subject = this@Publish.subject.orNull
            message = this@Publish.message.get()
            messageGroupId = this@Publish.messageGroupId.orNull
            messageDeduplicationId = this@Publish.messageDeduplicationId.orNull
        }

        runBlocking {
            service.get().getClient().publish(request)
        }
    }
}
