package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.azure.storage.blob.BlobContainerAsyncClientInfo
import com.kelvsyc.gradle.azure.storage.blob.BlobContainerClientInfo
import com.kelvsyc.gradle.azure.storage.blob.BlobServiceAsyncClientInfo
import com.kelvsyc.gradle.azure.storage.blob.BlobServiceClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.azure.storage.blob.BlobContainerAsyncClientInfoInternal
import com.kelvsyc.gradle.internal.azure.storage.blob.BlobContainerClientInfoInternal
import com.kelvsyc.gradle.internal.azure.storage.blob.BlobServiceAsyncClientInfoInternal
import com.kelvsyc.gradle.internal.azure.storage.blob.BlobServiceClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

class AzureBlobStorageBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().apply {
            registerBinding(BlobServiceClientInfo::class, BlobServiceClientInfoInternal::class)
            registerBinding(BlobServiceAsyncClientInfo::class, BlobServiceAsyncClientInfoInternal::class)
            registerBinding(BlobContainerClientInfo::class, BlobContainerClientInfoInternal::class)
            registerBinding(BlobContainerAsyncClientInfo::class, BlobContainerAsyncClientInfoInternal::class)
        }
    }
}
