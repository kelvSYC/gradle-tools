package com.kelvsyc.gradle.aws.kotlin.sts

import aws.sdk.kotlin.services.sts.StsClient
import com.kelvsyc.gradle.aws.kotlin.AbstractAwsKotlinClientBuildService
import com.kelvsyc.gradle.aws.kotlin.AwsBuildServiceParams

/**
 * Build service managing an [StsClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * the inherited [AwsBuildServiceParams] using the supplied extension functions
 * (e.g. [com.kelvsyc.gradle.aws.kotlin.defaultCredentials],
 * [com.kelvsyc.gradle.aws.kotlin.staticCredentials]). The same registration can then be shared with
 * value sources and work actions via a `Property<StsClientBuildService>` parameter.
 */
abstract class StsClientBuildService :
    AbstractAwsKotlinClientBuildService<StsClient, AwsBuildServiceParams>() {
    override fun createClient(): StsClient = StsClient {
        resolveRegion()?.let { region = it }
        resolveCredentialsProvider()?.let { credentialsProvider = it }
    }
}
