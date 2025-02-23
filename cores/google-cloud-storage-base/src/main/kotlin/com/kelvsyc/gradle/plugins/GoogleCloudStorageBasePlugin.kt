package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.google.cloud.storage.BatchDownloadFromGCS
import com.kelvsyc.gradle.google.cloud.storage.StorageClientInfo
import com.kelvsyc.gradle.internal.google.cloud.storage.StorageClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

class GoogleCloudStorageBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().registerBinding(StorageClientInfo::class, StorageClientInfoInternal::class)

        project.tasks.withType<BatchDownloadFromGCS>().configureEach {
            client.set(clientsService.zip(clientName, ClientsBaseService::getClient))
            client.disallowChanges()
            client.finalizeValueOnRead()
        }
    }
}
