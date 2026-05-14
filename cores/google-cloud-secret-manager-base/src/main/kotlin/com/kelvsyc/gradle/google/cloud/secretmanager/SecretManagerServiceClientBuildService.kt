package com.kelvsyc.gradle.google.cloud.secretmanager

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings
import com.kelvsyc.gradle.google.cloud.AbstractGcpClientBuildService
import com.kelvsyc.gradle.google.cloud.GcpBuildServiceParams

/**
 * Build service managing a [SecretManagerServiceClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring the credential source via the extension functions on [GcpBuildServiceParams] (e.g.
 * [applicationDefault][com.kelvsyc.gradle.google.cloud.applicationDefault],
 * [serviceAccount][com.kelvsyc.gradle.google.cloud.serviceAccount]). The same registration can
 * then be shared with value sources and work actions via a
 * `Property<SecretManagerServiceClientBuildService>` parameter.
 */
abstract class SecretManagerServiceClientBuildService :
    AbstractGcpClientBuildService<SecretManagerServiceClient, GcpBuildServiceParams>() {

    override fun createClient(): SecretManagerServiceClient {
        val settings = SecretManagerServiceSettings.newBuilder().apply {
            resolveCredentialsProvider()?.let { credentialsProvider = it }
        }.build()
        return SecretManagerServiceClient.create(settings)
    }
}
