package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.aws.java.secretsmanager.SecretsManagerAsyncClientInfo
import com.kelvsyc.gradle.aws.java.secretsmanager.SecretsManagerClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.secretsmanager.SecretsManagerAsyncClientInfoInternal
import com.kelvsyc.gradle.internal.aws.java.secretsmanager.SecretsManagerClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

class SecretsManagerJavaBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().registerBinding(SecretsManagerClientInfo::class, SecretsManagerClientInfoInternal::class)
        extension.service.get().registerBinding(SecretsManagerAsyncClientInfo::class, SecretsManagerAsyncClientInfoInternal::class)
    }
}
