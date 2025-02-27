package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.aws.java.s3.S3AsyncClientInfo
import com.kelvsyc.gradle.aws.java.s3.S3ClientInfo
import com.kelvsyc.gradle.aws.java.s3.S3TransferManagerClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.s3.S3AsyncClientInfoInternal
import com.kelvsyc.gradle.internal.aws.java.s3.S3ClientInfoInternal
import com.kelvsyc.gradle.internal.aws.java.s3.S3TransferManagerClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

class S3JavaBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().registerBinding(S3ClientInfo::class, S3ClientInfoInternal::class)
        extension.service.get().registerBinding(S3AsyncClientInfo::class, S3AsyncClientInfoInternal::class)
        extension.service.get()
            .registerBinding(S3TransferManagerClientInfo::class, S3TransferManagerClientInfoInternal::class)
    }
}
