package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.aws.java.sns.SnsAsyncClientInfo
import com.kelvsyc.gradle.aws.java.sns.SnsClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.sns.SnsAsyncClientInfoInternal
import com.kelvsyc.gradle.internal.aws.java.sns.SnsClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

class SnsJavaBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().registerBinding(SnsClientInfo::class, SnsClientInfoInternal::class)
        extension.service.get().registerBinding(SnsAsyncClientInfo::class, SnsAsyncClientInfoInternal::class)
    }
}
