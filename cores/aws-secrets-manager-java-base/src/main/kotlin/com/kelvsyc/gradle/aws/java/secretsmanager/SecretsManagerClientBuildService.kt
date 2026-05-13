package com.kelvsyc.gradle.aws.java.secretsmanager

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient

/**
 * Build service managing a synchronous [SecretsManagerClient] instance with config-cache-safe parameters.
 *
 * Configure region and credentials via the [AwsBuildServiceParams] extension functions:
 * ```kotlin
 * gradle.sharedServices.registerIfAbsent("secretsManager", SecretsManagerClientBuildService::class) {
 *     parameters {
 *         regionId.set("us-east-1")
 *         defaultCredentials()
 *     }
 * }
 * ```
 */
abstract class SecretsManagerClientBuildService :
    AbstractAwsJavaClientBuildService<SecretsManagerClient, AwsBuildServiceParams>() {
    override fun createClient(): SecretsManagerClient = configureBuilder(SecretsManagerClient.builder()).build()
}

