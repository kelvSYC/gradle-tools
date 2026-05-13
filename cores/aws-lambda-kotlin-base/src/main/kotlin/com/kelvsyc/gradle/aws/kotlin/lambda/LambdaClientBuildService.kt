package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import com.kelvsyc.gradle.aws.kotlin.AbstractAwsKotlinClientBuildService
import com.kelvsyc.gradle.aws.kotlin.AwsBuildServiceParams

/**
 * Build service managing a [LambdaClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * the inherited [AwsBuildServiceParams] using the supplied extension functions
 * (e.g. [com.kelvsyc.gradle.aws.kotlin.defaultCredentials],
 * [com.kelvsyc.gradle.aws.kotlin.staticCredentials]). The same registration can then be shared with
 * tasks, work actions and value sources via a `Property<LambdaClientBuildService>` parameter.
 */
abstract class LambdaClientBuildService :
    AbstractAwsKotlinClientBuildService<LambdaClient, AwsBuildServiceParams>() {
    override fun createClient(): LambdaClient = LambdaClient {
        resolveRegion()?.let { region = it }
        resolveCredentialsProvider()?.let { credentialsProvider = it }
    }
}
