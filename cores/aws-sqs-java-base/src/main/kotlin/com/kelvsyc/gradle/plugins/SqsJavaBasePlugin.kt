package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.aws.java.sqs.SendMessageBatch
import com.kelvsyc.gradle.aws.java.sqs.SqsAsyncClientInfo
import com.kelvsyc.gradle.aws.java.sqs.SqsClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.internal.aws.java.sqs.SqsAsyncClientInfoInternal
import com.kelvsyc.gradle.internal.aws.java.sqs.SqsClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

class SqsJavaBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().registerBinding(SqsClientInfo::class, SqsClientInfoInternal::class)
        extension.service.get().registerBinding(SqsAsyncClientInfo::class, SqsAsyncClientInfoInternal::class)

        project.tasks.withType<SendMessageBatch>().configureEach {
            client.set(clientsService.zip(clientName, ClientsBaseService::getClient))
            client.disallowChanges()
            client.finalizeValueOnRead()
        }
    }
}
