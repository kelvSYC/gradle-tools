package com.kelvsyc.gradle.google.cloud.secretmanager

import com.google.cloud.secretmanager.v1.ListSecretsRequest
import com.google.cloud.secretmanager.v1.ProjectName
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

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
        /** The shared build service managing Secret Manager clients. */
        val service: Property<ClientsBaseService>

        /** Registered name of a [SecretManagerClientInfo]. */
        val clientName: Property<String>

        /** GCP project ID. */
        val projectId: Property<String>
    }

    private val client: Provider<SecretManagerServiceClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): List<String>? {
        val parent = ProjectName.of(parameters.projectId.get()).toString()
        val request = ListSecretsRequest.newBuilder().apply {
            this.parent = parent
        }.build()
        return client.get().listSecrets(request).iterateAll().map { it.name }
    }
}
