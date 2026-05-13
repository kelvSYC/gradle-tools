package com.kelvsyc.gradle.aws.java.secretsmanager

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient

/**
 * Build service managing an asynchronous [SecretsManagerAsyncClient] instance with config-cache-safe parameters.
 *
 * Configure region and credentials via the [AwsBuildServiceParams] extension functions:
 * ```kotlin
 * gradle.sharedServices.registerIfAbsent("secretsManager-async", SecretsManagerAsyncClientBuildService::class) {
 *     parameters {
 *         regionId.set("us-east-1")
 *         defaultCredentials()
 *     }
 * }
 * ```
 */
abstract class SecretsManagerAsyncClientBuildService :
    AbstractAwsJavaClientBuildService<SecretsManagerAsyncClient, AwsBuildServiceParams>() {
    override fun createClient(): SecretsManagerAsyncClient = configureBuilder(SecretsManagerAsyncClient.builder()).build()
}

