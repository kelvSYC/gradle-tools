package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.cloud.kms.v1.KeyManagementServiceSettings
import com.kelvsyc.gradle.google.cloud.AbstractGcpClientBuildService
import com.kelvsyc.gradle.google.cloud.GcpBuildServiceParams

/**
 * Build service managing a [KeyManagementServiceClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring the credential source via the extension functions on [GcpBuildServiceParams] (e.g.
 * [applicationDefault][com.kelvsyc.gradle.google.cloud.applicationDefault],
 * [serviceAccount][com.kelvsyc.gradle.google.cloud.serviceAccount]). The same registration can
 * then be shared with value sources and work actions via a
 * `Property<KmsClientBuildService>` parameter.
 */
abstract class KmsClientBuildService :
    AbstractGcpClientBuildService<KeyManagementServiceClient, GcpBuildServiceParams>() {

    override fun createClient(): KeyManagementServiceClient {
        val settings = KeyManagementServiceSettings.newBuilder().apply {
            resolveCredentialsProvider()?.let { credentialsProvider = it }
        }.build()
        return KeyManagementServiceClient.create(settings)
    }
}
