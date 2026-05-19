package com.kelvsyc.gradle.google.cloud.functions

import com.google.cloud.functions.v2.FunctionServiceClient
import com.google.cloud.functions.v2.FunctionServiceSettings
import com.kelvsyc.gradle.google.cloud.AbstractGcpClientBuildService
import com.kelvsyc.gradle.google.cloud.GcpBuildServiceParams

/**
 * Build service managing a [FunctionServiceClient] instance for the Cloud Functions Gen 2 API.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring the credential source via the extension functions on [GcpBuildServiceParams] (e.g.
 * [applicationDefault][com.kelvsyc.gradle.google.cloud.applicationDefault],
 * [serviceAccount][com.kelvsyc.gradle.google.cloud.serviceAccount]). The same registration can
 * then be shared with value sources, work actions, and tasks via a
 * `Property<FunctionServiceClientBuildService>` parameter.
 */
abstract class FunctionServiceClientBuildService :
    AbstractGcpClientBuildService<FunctionServiceClient, GcpBuildServiceParams>() {

    override fun createClient(): FunctionServiceClient {
        val settings = FunctionServiceSettings.newBuilder().apply {
            resolveCredentialsProvider()?.let { credentialsProvider = it }
        }.build()
        return FunctionServiceClient.create(settings)
    }
}
