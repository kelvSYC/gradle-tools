package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.bitbucket.cloud.BitbucketCloudClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.bitbucket.cloud.BitbucketCloudClientInfoInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

/**
 * Plugin that registers a [BitbucketCloudClientInfo] binding in the [ClientsBaseExtension] service registry.
 *
 * Apply this plugin to enable Bitbucket Cloud API access from ValueSources, WorkActions, and tasks.
 */
class BitbucketCloudBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.kelvsyc.gradle.clients-base")

        val extension = project.the<ClientsBaseExtension>()
        extension.service.get().registerBinding(
            BitbucketCloudClientInfo::class,
            BitbucketCloudClientInfoInternal::class,
        )
    }
}
