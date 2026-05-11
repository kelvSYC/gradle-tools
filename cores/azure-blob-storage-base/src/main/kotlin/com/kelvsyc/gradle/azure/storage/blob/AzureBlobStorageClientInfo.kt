package com.kelvsyc.gradle.azure.storage.blob

import com.azure.core.credential.TokenCredential
import com.kelvsyc.gradle.clients.ServiceClientInfo
import org.gradle.api.provider.Property

/**
 * Base configuration interface for Azure Blob Storage clients.
 *
 * @param T The Azure Blob Storage client type
 */
interface AzureBlobStorageClientInfo<T : Any> : ServiceClientInfo<T> {
    /**
     * The Azure Storage account endpoint URL, e.g. `https://{accountName}.blob.core.windows.net`.
     */
    val endpoint: Property<String>

    /**
     * The credentials used to authenticate with Azure Blob Storage.
     *
     * If absent, the underlying client will use no authentication. Set to
     * `DefaultAzureCredentialBuilder().build()` (from `com.azure:azure-identity`) to use Azure's default credential
     * chain.
     */
    val credential: Property<TokenCredential>
}
