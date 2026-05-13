package com.kelvsyc.gradle.artifactory

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.ArtifactoryClientBuilder

/**
 * Build service managing an [Artifactory] client instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.url] and [Params.credentials] as needed. The same registration can then be shared with
 * value sources, work actions and tasks via a `Property<ArtifactoryClientBuildService>` parameter.
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
         * The credentials used to authenticate with Artifactory.
         */
        val credentials: Property<PasswordCredentials>
    }

    override fun createClient(): Artifactory = ArtifactoryClientBuilder.create().apply {
        url = parameters.url.get()
        username = parameters.credentials.get().username
        password = parameters.credentials.get().password
    }.build()
}
