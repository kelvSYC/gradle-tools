package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.aws.internal.java.imds.ImdsAsyncClientInfoInternal
import com.kelvsyc.gradle.aws.internal.java.imds.ImdsClientInfoInternal
import com.kelvsyc.gradle.aws.java.imds.ImdsAsyncClientInfo
import com.kelvsyc.gradle.aws.java.imds.ImdsClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

class ImdsJavaBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().registerBinding(ImdsClientInfo::class, ImdsClientInfoInternal::class)
        extension.service.get().registerBinding(ImdsAsyncClientInfo::class, ImdsAsyncClientInfoInternal::class)
    }
}
