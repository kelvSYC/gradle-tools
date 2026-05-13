package com.kelvsyc.gradle.google.cloud.artifact

import com.google.api.gax.core.CredentialsProvider
import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.ArtifactRegistrySettings
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing an [ArtifactRegistryClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.credentials] as needed. The same registration can then be shared with value sources and work
 * actions via a `Property<ArtifactRegistryClientBuildService>` parameter.
 */
abstract class ArtifactRegistryClientBuildService :
    AbstractClientBuildService<ArtifactRegistryClient, ArtifactRegistryClientBuildService.Params>() {
    /**
     * Configuration parameters for [ArtifactRegistryClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The Google API [CredentialsProvider] used for authentication.
         *
         * If unset, the underlying client uses application default credentials.
         */
        val credentials: Property<CredentialsProvider>
    }

    override fun createClient(): ArtifactRegistryClient {
        val settings = ArtifactRegistrySettings.newBuilder().apply {
            if (parameters.credentials.isPresent) {
                credentialsProvider = parameters.credentials.get()
            }
        }.build()
        return ArtifactRegistryClient.create(settings)
    }
}
