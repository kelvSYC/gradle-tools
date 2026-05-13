package com.kelvsyc.gradle.aws.kotlin.sqs

import aws.sdk.kotlin.services.sqs.SqsClient
import com.kelvsyc.gradle.aws.kotlin.AbstractAwsKotlinClientBuildService
import com.kelvsyc.gradle.aws.kotlin.AwsBuildServiceParams

/**
 * Build service managing an [SqsClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * the inherited [AwsBuildServiceParams] using the supplied extension functions
 * (e.g. [com.kelvsyc.gradle.aws.kotlin.defaultCredentials],
 * [com.kelvsyc.gradle.aws.kotlin.staticCredentials]). The same registration can then be shared with
 * tasks and work actions via a `Property<SqsClientBuildService>` parameter.
 */
abstract class SqsClientBuildService :
    AbstractAwsKotlinClientBuildService<SqsClient, AwsBuildServiceParams>() {
    override fun createClient(): SqsClient = SqsClient {
        resolveRegion()?.let { region = it }
        resolveCredentialsProvider()?.let { credentialsProvider = it }
    }
}
