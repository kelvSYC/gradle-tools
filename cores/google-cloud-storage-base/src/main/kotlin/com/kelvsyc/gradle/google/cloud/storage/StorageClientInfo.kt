package com.kelvsyc.gradle.google.cloud.storage

import com.google.auth.Credentials
import com.google.cloud.storage.Storage
import com.kelvsyc.gradle.clients.ServiceClientInfo
import org.gradle.api.provider.Property

interface StorageClientInfo : ServiceClientInfo<Storage> {
    /**
     * The GCP project ID.
     */
    val projectId: Property<String>

    /**
     * The credentials used to access Google Cloud Storage.
     *
     * If absent, the underlying client will use no authentication rather than the default authentication. Set to
     * [GoogleCredentials.getApplicationDefault][com.google.auth.oauth2.GoogleCredentials.getApplicationDefault] to use
     * the default authentication.
     */
    val credentials: Property<Credentials>
}
