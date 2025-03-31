package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.aws.java.ses.SesAsyncClientInfo
import com.kelvsyc.gradle.aws.java.ses.SesClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.ses.SesAsyncClientInfoInternal
import com.kelvsyc.gradle.internal.aws.java.ses.SesClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

class SesJavaBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().registerBinding(SesClientInfo::class, SesClientInfoInternal::class)
        extension.service.get().registerBinding(SesAsyncClientInfo::class, SesAsyncClientInfoInternal::class)
    }
}
