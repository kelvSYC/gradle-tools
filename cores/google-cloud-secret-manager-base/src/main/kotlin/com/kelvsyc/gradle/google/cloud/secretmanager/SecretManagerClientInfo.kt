package com.kelvsyc.gradle.google.cloud.secretmanager

import com.google.auth.Credentials
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.kelvsyc.gradle.clients.ServiceClientInfo
import org.gradle.api.provider.Property

interface SecretManagerClientInfo : ServiceClientInfo<SecretManagerServiceClient> {
    /**
     * The GCP project ID.
     */
    val projectId: Property<String>

    /**
     * The credentials used to access Google Cloud Secret Manager.
     *
     * If absent, the underlying client will use application default credentials.
     */
    val credentials: Property<Credentials>
}
