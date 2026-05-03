package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.aws.kotlin.ses.SendBulkTemplatedMail
import com.kelvsyc.gradle.aws.kotlin.ses.SesClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.internal.aws.kotlin.ses.SesClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

abstract class SesKotlinBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().registerBinding(SesClientInfo::class, SesClientInfoInternal::class)

        project.tasks.withType<SendBulkTemplatedMail>().configureEach {
            client.set(service.zip(clientName, ClientsBaseService::getClient))
            client.disallowChanges()
            client.finalizeValueOnRead()
        }
    }
}
