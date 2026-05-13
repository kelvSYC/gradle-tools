package com.kelvsyc.gradle.google.cloud.secretmanager

import com.google.cloud.secretmanager.v1.ListSecretsRequest
import com.google.cloud.secretmanager.v1.ProjectName
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a list of secret resource names within a GCP project.
 *
 * Pagination is handled internally via the high-level paged API.
 *
 * Each entry is the fully-qualified resource name in the form `projects/{project}/secrets/{secret}`.
 */
abstract class ListSecretsValueSource : ValueSource<List<String>, ListSecretsValueSource.Parameters> {
    /**
     * Parameters for [ListSecretsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the Secret Manager client. */
        @get:Internal
        val service: Property<SecretManagerServiceClientBuildService>

        /** GCP project ID. */
        val projectId: Property<String>
    }

    override fun obtain(): List<String>? {
        val parent = ProjectName.of(parameters.projectId.get()).toString()
        val request = ListSecretsRequest.newBuilder().apply {
            this.parent = parent
        }.build()
        return parameters.service.get().getClient().listSecrets(request).iterateAll().map { it.name }
    }
}
