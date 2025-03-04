package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.google.cloud.artifact.ArtifactRegistryClientInfo
import com.kelvsyc.gradle.internal.google.cloud.artifact.ArtifactRegistryClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

class GoogleCloudArtifactRegistryBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get()
            .registerBinding(ArtifactRegistryClientInfo::class, ArtifactRegistryClientInfoInternal::class)
    }
}
