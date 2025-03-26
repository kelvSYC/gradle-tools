package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.aws.kotlin.sns.SnsClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.sns.SnsClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

class SnsKotlinBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().registerBinding(SnsClientInfo::class, SnsClientInfoInternal::class)
    }
}
