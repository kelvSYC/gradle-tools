package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.aws.kotlin.s3.BatchDownloadFromS3
import com.kelvsyc.gradle.aws.kotlin.s3.BatchUploadToS3
import com.kelvsyc.gradle.aws.kotlin.s3.S3ClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.internal.aws.kotlin.s3.S3ClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

class S3KotlinBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().registerBinding(S3ClientInfo::class, S3ClientInfoInternal::class)

        project.tasks.withType<BatchDownloadFromS3>().configureEach {
            client.set(service.zip(clientName, ClientsBaseService::getClient))
            client.disallowChanges()
            client.finalizeValueOnRead()
        }
        project.tasks.withType<BatchUploadToS3>().configureEach {
            client.set(service.zip(clientName, ClientsBaseService::getClient))
            client.disallowChanges()
            client.finalizeValueOnRead()
        }
    }
}
