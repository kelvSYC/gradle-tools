package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.azure.keyvault.SecretAsyncClientInfo
import com.kelvsyc.gradle.azure.keyvault.SecretClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.azure.keyvault.SecretAsyncClientInfoInternal
import com.kelvsyc.gradle.internal.azure.keyvault.SecretClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

class AzureKeyVaultBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().apply {
            registerBinding(SecretClientInfo::class, SecretClientInfoInternal::class)
            registerBinding(SecretAsyncClientInfo::class, SecretAsyncClientInfoInternal::class)
        }
    }
}
