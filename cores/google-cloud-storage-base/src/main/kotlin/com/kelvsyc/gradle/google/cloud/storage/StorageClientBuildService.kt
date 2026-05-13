package com.kelvsyc.gradle.google.cloud.storage

import com.google.auth.Credentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing a [Storage] client instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.projectId] and [Params.credentials] as needed. The same registration can then be shared with
 * value sources, work actions and tasks via a `Property<StorageClientBuildService>` parameter.
 */
abstract class StorageClientBuildService :
    AbstractClientBuildService<Storage, StorageClientBuildService.Params>() {
    /**
     * Configuration parameters for [StorageClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The GCP project ID.
         */
        val projectId: Property<String>

        /**
         * The credentials used to access Google Cloud Storage.
         *
         * If unset, the underlying client uses no authentication rather than the default authentication.
         * Set to
         * [GoogleCredentials.getApplicationDefault][com.google.auth.oauth2.GoogleCredentials.getApplicationDefault]
         * to use the default authentication.
         */
        val credentials: Property<Credentials>
    }

    override fun createClient(): Storage = StorageOptions.newBuilder().apply {
        setProjectId(parameters.projectId.get())
        if (parameters.credentials.isPresent) {
            setCredentials(parameters.credentials.get())
        }
    }.build().service
}
