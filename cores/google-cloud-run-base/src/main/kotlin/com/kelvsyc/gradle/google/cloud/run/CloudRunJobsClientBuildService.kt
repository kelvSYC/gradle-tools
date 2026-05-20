package com.kelvsyc.gradle.google.cloud.run

import com.google.cloud.run.v2.JobsClient
import com.google.cloud.run.v2.JobsSettings
import com.kelvsyc.gradle.google.cloud.AbstractGcpClientBuildService
import com.kelvsyc.gradle.google.cloud.GcpBuildServiceParams

/**
 * Build service managing a [JobsClient] instance for the Cloud Run v2 API.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring the credential source via the extension functions on [GcpBuildServiceParams] (e.g.
 * [applicationDefault][com.kelvsyc.gradle.google.cloud.applicationDefault],
 * [serviceAccount][com.kelvsyc.gradle.google.cloud.serviceAccount]). The same registration can
 * then be shared with value sources, work actions, and tasks via a
 * `Property<CloudRunJobsClientBuildService>` parameter.
 */
abstract class CloudRunJobsClientBuildService :
    AbstractGcpClientBuildService<JobsClient, GcpBuildServiceParams>() {

    /**
     * Creates a [JobsClient] configured with the resolved credentials provider (if present).
     *
     * @return the initialized client
     */
    override fun createClient(): JobsClient {
        val settings = JobsSettings.newBuilder().apply {
            resolveCredentialsProvider()?.let { credentialsProvider = it }
        }.build()
        return JobsClient.create(settings)
    }
}
