package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.aws.java.ecr.EcrClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.ecr.EcrClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

class EcrJavaBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().registerBinding(EcrClientInfo::class, EcrClientInfoInternal::class)
    }
}
