package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.google.cloud.secretmanager.SecretManagerClientInfo
import com.kelvsyc.gradle.internal.google.cloud.secretmanager.SecretManagerClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

class GoogleCloudSecretManagerBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().registerBinding(SecretManagerClientInfo::class, SecretManagerClientInfoInternal::class)
    }
}
