package com.kelvsyc.gradle.google.cloud.pubsub

import com.google.pubsub.v1.ListTopicsRequest
import com.google.pubsub.v1.ProjectName
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a list of topic resource names within a GCP project.
 *
 * Pagination is handled internally via the high-level paged API.
 *
 * Each entry is the fully-qualified resource name in the form `projects/{project}/topics/{topic}`.
 */
abstract class ListTopicsValueSource : ValueSource<List<String>, ListTopicsValueSource.Parameters> {
    /**
     * Parameters for [ListTopicsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the Pub/Sub client. */
        @get:Internal
        val service: Property<TopicAdminClientBuildService>

        /** GCP project ID. */
        val projectId: Property<String>
    }

    override fun obtain(): List<String>? {
        val parent = ProjectName.of(parameters.projectId.get()).toString()
        val request = ListTopicsRequest.newBuilder().apply {
            project = parent
        }.build()
        return parameters.service.get().getClient().listTopics(request).iterateAll().map { it.name }
    }
}
