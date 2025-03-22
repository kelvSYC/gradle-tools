package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.aws.kotlin.codeartifact.CodeArtifactClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.codeartifact.CodeArtifactClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

class CodeArtifactKotlinBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().registerBinding(CodeArtifactClientInfo::class, CodeArtifactClientInfoInternal::class)
    }
}
