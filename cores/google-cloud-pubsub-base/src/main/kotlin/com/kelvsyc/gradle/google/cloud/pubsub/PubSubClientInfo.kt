package com.kelvsyc.gradle.google.cloud.pubsub

import com.google.api.gax.core.CredentialsProvider
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.kelvsyc.gradle.clients.ServiceClientInfo
import org.gradle.api.provider.Property

/**
 * Client info for Google Cloud Pub/Sub.
 *
 * The registered client is a [TopicAdminClient], which supports both topic administration (list, create,
 * delete topics) and message publishing.
 */
interface PubSubClientInfo : ServiceClientInfo<TopicAdminClient> {
    /** Google API [CredentialsProvider] for authentication. */
    val credentials: Property<CredentialsProvider>
}
