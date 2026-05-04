package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.google.cloud.pubsub.PubSubClientInfo
import com.kelvsyc.gradle.google.cloud.pubsub.PublishBatch
import com.kelvsyc.gradle.internal.google.cloud.pubsub.PubSubClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

class GoogleCloudPubSubBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get()
            .registerBinding(PubSubClientInfo::class, PubSubClientInfoInternal::class)

        project.tasks.withType<PublishBatch>().configureEach {
            client.set(service.zip(clientName, ClientsBaseService::getClient))
            client.disallowChanges()
            client.finalizeValueOnRead()
        }
    }
}
