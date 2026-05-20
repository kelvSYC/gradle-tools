package com.kelvsyc.gradle.google.cloud.run

import com.google.cloud.run.v2.ServicesClient
import com.google.cloud.run.v2.ServicesSettings
import com.kelvsyc.gradle.google.cloud.AbstractGcpClientBuildService
import com.kelvsyc.gradle.google.cloud.GcpBuildServiceParams

/**
 * Build service managing a [ServicesClient] instance for the Cloud Run v2 API.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring the credential source via the extension functions on [GcpBuildServiceParams] (e.g.
 * [applicationDefault][com.kelvsyc.gradle.google.cloud.applicationDefault],
 * [serviceAccount][com.kelvsyc.gradle.google.cloud.serviceAccount]). The same registration can
 * then be shared with value sources, work actions, and tasks via a
 * `Property<CloudRunServicesClientBuildService>` parameter.
 */
abstract class CloudRunServicesClientBuildService :
    AbstractGcpClientBuildService<ServicesClient, GcpBuildServiceParams>() {

    /**
     * Creates a [ServicesClient] configured with the resolved credentials provider (if present).
     *
     * @return the initialized client
     */
    override fun createClient(): ServicesClient {
        val settings = ServicesSettings.newBuilder().apply {
            resolveCredentialsProvider()?.let { credentialsProvider = it }
        }.build()
        return ServicesClient.create(settings)
    }
}
