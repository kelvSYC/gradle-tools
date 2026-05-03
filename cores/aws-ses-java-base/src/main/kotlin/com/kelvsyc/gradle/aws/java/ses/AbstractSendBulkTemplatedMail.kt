package com.kelvsyc.gradle.aws.java.ses

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import org.gradle.work.DisableCachingByDefault
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.BulkEmailDestination
import software.amazon.awssdk.services.ses.model.BulkEmailStatus
import software.amazon.awssdk.services.ses.model.Destination
import software.amazon.awssdk.services.ses.model.SendBulkTemplatedEmailRequest
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
    private val objects: ObjectFactory,
    private val providers: ProviderFactory
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
    abstract class Entry @Inject constructor(private val name: String) : Named {
        override fun getName() = name

        /**
         * To addresses for this destination.
         */
        abstract val recipients: ListProperty<String>

        /**
         * CC addresses for this destination.
         */
        abstract val ccAddresses: ListProperty<String>

        /**
         * BCC addresses for this destination.
         */
        abstract val bccAddresses: ListProperty<String>

        /**
         * Per-destination replacement template data, overriding [defaultTemplateData].
         * Must be a JSON object expressed as a string.
         */
        abstract val templateData: Property<String>
    }

    /**
     * Entries to be sent. Users normally add to this collection through [registerEntry].
     */
    @get:Internal
    abstract val entries: MapProperty<String, Entry>

    /**
     * Registers a new destination entry. The supplied [name] serves as a logical identifier.
     */
    fun registerEntry(name: String, configureAction: Action<in Entry>) {
        entries.put(name, providers.provider {
            objects.newInstance<Entry>(name).also { configureAction.execute(it) }
        })
    }

    private fun toDestination(entry: Entry): BulkEmailDestination =
        BulkEmailDestination.builder().apply {
            destination(
                Destination.builder().apply {
                    toAddresses(entry.recipients.getOrElse(emptyList()))
                    ccAddresses(entry.ccAddresses.getOrElse(emptyList()))
                    bccAddresses(entry.bccAddresses.getOrElse(emptyList()))
                }.build()
            )
            entry.templateData.orNull?.let { replacementTemplateData(it) }
        }.build()

    @TaskAction
    fun run() {
        val ses = client.get()
        val allEntries = entries.get()
        val names = allEntries.keys.toList()
        val destinations = allEntries.values.map(::toDestination)

        val failed = mutableListOf<String>()
        names.zip(destinations).chunked(SES_BULK_BATCH_SIZE).forEach { chunk ->
            val chunkNames = chunk.map { it.first }
            val chunkDestinations = chunk.map { it.second }

            val request = SendBulkTemplatedEmailRequest.builder().apply {
                source(sender.get())
                template(templateName.get())
                defaultTemplateData.orNull?.let { defaultTemplateData(it) }
                destinations(chunkDestinations)
            }.build()

            val response = ses.sendBulkTemplatedEmail(request)
            response.status().forEachIndexed { index, status ->
                if (status.status() != BulkEmailStatus.SUCCESS) {
                    failed += chunkNames[index]
                }
            }
        }

        if (failed.isNotEmpty()) {
            error("SES bulk templated email failed for entries: ${failed.joinToString()}")
        }
    }

    private companion object {
        private const val SES_BULK_BATCH_SIZE = 50
    }
}
