package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.ArtifactRegistrySettings
import com.kelvsyc.gradle.google.cloud.AbstractGcpClientBuildService
import com.kelvsyc.gradle.google.cloud.GcpBuildServiceParams

/**
 * Build service managing an [ArtifactRegistryClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring the credential source via the extension functions on [GcpBuildServiceParams] (e.g.
 * [applicationDefault][com.kelvsyc.gradle.google.cloud.applicationDefault],
 * [serviceAccount][com.kelvsyc.gradle.google.cloud.serviceAccount]). The same registration can
 * then be shared with value sources and work actions via a
 * `Property<ArtifactRegistryClientBuildService>` parameter.
 */
abstract class ArtifactRegistryClientBuildService :
    AbstractGcpClientBuildService<ArtifactRegistryClient, GcpBuildServiceParams>() {

    override fun createClient(): ArtifactRegistryClient {
        val settings = ArtifactRegistrySettings.newBuilder().apply {
            resolveCredentialsProvider()?.let { credentialsProvider = it }
        }.build()
        return ArtifactRegistryClient.create(settings)
    }
}
