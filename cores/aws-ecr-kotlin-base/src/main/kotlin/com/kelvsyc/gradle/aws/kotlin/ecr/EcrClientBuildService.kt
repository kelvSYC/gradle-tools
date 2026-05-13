package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.EcrClient
import com.kelvsyc.gradle.aws.kotlin.AbstractAwsKotlinClientBuildService
import com.kelvsyc.gradle.aws.kotlin.AwsBuildServiceParams

/**
 * Build service managing an [EcrClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * the inherited [AwsBuildServiceParams] using the supplied extension functions
 * (e.g. [com.kelvsyc.gradle.aws.kotlin.defaultCredentials],
 * [com.kelvsyc.gradle.aws.kotlin.staticCredentials]). The same registration can then be shared with
 * tasks, work actions and value sources via a `Property<EcrClientBuildService>` parameter.
 */
abstract class EcrClientBuildService :
    AbstractAwsKotlinClientBuildService<EcrClient, AwsBuildServiceParams>() {
    override fun createClient(): EcrClient = EcrClient {
        resolveRegion()?.let { region = it }
        resolveCredentialsProvider()?.let { credentialsProvider = it }
    }
}
