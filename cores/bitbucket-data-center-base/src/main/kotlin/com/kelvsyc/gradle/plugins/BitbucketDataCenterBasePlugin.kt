package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.bitbucket.server.BitbucketServerClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.bitbucket.server.BitbucketServerClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

/**
 * Plugin that registers a [BitbucketServerClientInfo] binding in the [ClientsBaseExtension] service registry.
 *
 * Apply this plugin to enable Bitbucket Data Center API access from ValueSources, WorkActions, and tasks.
 */
class BitbucketDataCenterBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().registerBinding(
            BitbucketServerClientInfo::class,
            BitbucketServerClientInfoInternal::class,
        )
    }
}
