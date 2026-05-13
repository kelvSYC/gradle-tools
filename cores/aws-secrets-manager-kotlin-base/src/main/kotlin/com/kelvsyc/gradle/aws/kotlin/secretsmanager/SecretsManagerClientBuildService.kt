package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import com.kelvsyc.gradle.aws.kotlin.AbstractAwsKotlinClientBuildService
import com.kelvsyc.gradle.aws.kotlin.AwsBuildServiceParams

/**
 * Build service managing a [SecretsManagerClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * the inherited [AwsBuildServiceParams] using the supplied extension functions
 * (e.g. [com.kelvsyc.gradle.aws.kotlin.defaultCredentials],
 * [com.kelvsyc.gradle.aws.kotlin.staticCredentials]). The same registration can then be shared with
 * tasks, work actions and value sources via a `Property<SecretsManagerClientBuildService>` parameter.
 */
abstract class SecretsManagerClientBuildService :
    AbstractAwsKotlinClientBuildService<SecretsManagerClient, AwsBuildServiceParams>() {
    override fun createClient(): SecretsManagerClient = SecretsManagerClient {
        resolveRegion()?.let { region = it }
        resolveCredentialsProvider()?.let { credentialsProvider = it }
    }
}
