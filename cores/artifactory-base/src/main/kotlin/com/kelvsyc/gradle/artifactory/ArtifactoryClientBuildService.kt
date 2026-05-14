package com.kelvsyc.gradle.artifactory

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.ArtifactoryClientBuilder

/**
 * Build service managing an [Artifactory] client instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.url], [Params.username], and [Params.password] as needed. The same registration can then be
 * shared with value sources, work actions and tasks via a `Property<ArtifactoryClientBuildService>` parameter.
 */
abstract class ArtifactoryClientBuildService :
    AbstractClientBuildService<Artifactory, ArtifactoryClientBuildService.Params>() {
    /**
     * Configuration parameters for [ArtifactoryClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The Artifactory server URL (e.g. `https://mycompany.jfrog.io/artifactory`).
         */
        val url: Property<String>

        /**
         * The Artifactory username. Leave unset for anonymous access.
         */
        val username: Property<String>

        /**
         * The Artifactory password or API token. Leave unset for anonymous access.
         */
        val password: Property<String>
    }

    override fun createClient(): Artifactory = ArtifactoryClientBuilder.create().apply {
        url = parameters.url.get()
        username = parameters.username.orNull
        password = parameters.password.orNull
    }.build()
}
