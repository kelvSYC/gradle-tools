package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.artifactory.ArtifactoryClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.artifactory.ArtifactoryClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

class ArtifactoryBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().registerBinding(ArtifactoryClientInfo::class, ArtifactoryClientInfoInternal::class)
    }
}
