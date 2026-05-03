package com.kelvsyc.gradle.aws.kotlin.ses

import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.ses.model.BulkEmailDestination
import aws.sdk.kotlin.services.ses.model.BulkEmailStatus
import aws.sdk.kotlin.services.ses.model.Destination
import aws.sdk.kotlin.services.ses.model.SendBulkTemplatedEmailRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * Sends a templated email to an arbitrary number of destinations via the SES `SendBulkTemplatedEmail` API.
 * The task internally chunks entries into the maximum batch size supported by SES, so callers may register
 * any number of entries.
 *
 * Specify entries using [registerEntry]. Each entry's name serves as a logical identifier and must be unique
 * within the task. Per-entry failures within an otherwise-successful batch call are reported as a task failure
 * listing the failed entry names.
 */
@DisableCachingByDefault(because = "Sending email is not cacheable")
abstract class AbstractSendBulkTemplatedMail @Inject constructor(
    private val objects: ObjectFactory
) : DefaultTask() {
    /**
     * The [SesClient] used to send emails.
     */
    @get:Internal
    abstract val client: Property<SesClient>

    /**
     * The sender (From) email address.
     */
    @get:Input
    abstract val sender: Property<String>

    /**
     * SES template name.
     */
    @get:Input
    abstract val templateName: Property<String>

    /**
     * Default template data applied when a destination does not specify its own replacement data.
     * Must be a JSON object expressed as a string.
     */
    @get:Input
    @get:Optional
    abstract val defaultTemplateData: Property<String>

    /**
     * Information about a single destination in the bulk email.
     */
    interface Entry {
        /**
         * To addresses for this destination.
         */
        @get:Input
        val recipients: ListProperty<String>

        /**
         * CC addresses for this destination.
         */
        @get:Input
        @get:Optional
        val ccAddresses: ListProperty<String>

        /**
         * BCC addresses for this destination.
         */
        @get:Input
        @get:Optional
        val bccAddresses: ListProperty<String>

        /**
         * Per-destination replacement template data, overriding [defaultTemplateData].
         * Must be a JSON object expressed as a string.
         */
        @get:Input
        @get:Optional
        val templateData: Property<String>
    }

    /**
     * Entries to be sent. Users normally add to this collection through [registerEntry].
     */
    @get:Nested
    abstract val entries: MapProperty<String, Entry>

    /**
     * Registers a new destination entry. The supplied [name] serves as a logical identifier.
     */
    fun registerEntry(name: String, action: Action<in Entry>) {
        val entry = objects.newInstance<Entry>().also { action.execute(it) }
        entries.put(name, entry)
    }

    private fun toDestination(entry: Entry): BulkEmailDestination =
        BulkEmailDestination {
            destination = Destination {
                toAddresses = entry.recipients.getOrElse(emptyList())
                ccAddresses = entry.ccAddresses.getOrElse(emptyList())
                bccAddresses = entry.bccAddresses.getOrElse(emptyList())
            }
            replacementTemplateData = entry.templateData.orNull
        }

    @TaskAction
    fun run() {
        val ses = client.get()
        val allEntries = entries.get()
        val names = allEntries.keys.toList()
        val destinations = allEntries.values.map(::toDestination)

        val failed = mutableListOf<String>()
        runBlocking {
            names.zip(destinations).chunked(SES_BULK_BATCH_SIZE).forEach { chunk ->
                val chunkNames = chunk.map { it.first }
                val chunkDestinations = chunk.map { it.second }

                val request = SendBulkTemplatedEmailRequest {
                    source = sender.get()
                    template = templateName.get()
                    this.defaultTemplateData = this@AbstractSendBulkTemplatedMail.defaultTemplateData.orNull
                    this.destinations = chunkDestinations
                }

                val response = ses.sendBulkTemplatedEmail(request)
                response.status.forEachIndexed { index, status ->
                    if (status.status != BulkEmailStatus.Success) {
                        failed += chunkNames[index]
                    }
                }
            }
        }

        if (failed.isNotEmpty()) {
            throw GradleException("SES bulk templated email failed for entries: ${failed.joinToString()}")
        }
    }

    private companion object {
        private const val SES_BULK_BATCH_SIZE = 50
    }
}
