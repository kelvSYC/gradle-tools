package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.aws.kotlin.sqs.SendMessageBatch
import com.kelvsyc.gradle.aws.kotlin.sqs.SqsClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.internal.aws.kotlin.sqs.SqsClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

class SqsKotlinBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().registerBinding(SqsClientInfo::class, SqsClientInfoInternal::class)

        project.tasks.withType<SendMessageBatch>().configureEach {
            client.set(service.zip(clientName, ClientsBaseService::getClient))
            client.disallowChanges()
            client.finalizeValueOnRead()
        }
    }
}
