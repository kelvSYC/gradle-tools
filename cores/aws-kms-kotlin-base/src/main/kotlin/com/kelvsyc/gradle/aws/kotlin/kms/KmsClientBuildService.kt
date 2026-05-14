package com.kelvsyc.gradle.aws.kotlin.kms

import aws.sdk.kotlin.services.kms.KmsClient
import com.kelvsyc.gradle.aws.kotlin.AbstractAwsKotlinClientBuildService
import com.kelvsyc.gradle.aws.kotlin.AwsBuildServiceParams

/**
 * Build service managing a [KmsClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * the inherited [AwsBuildServiceParams] using the supplied extension functions
 * (e.g. [com.kelvsyc.gradle.aws.kotlin.defaultCredentials],
 * [com.kelvsyc.gradle.aws.kotlin.staticCredentials]). The same registration can then be shared with
 * tasks, work actions and value sources via a `Property<KmsClientBuildService>` parameter.
 */
abstract class KmsClientBuildService :
    AbstractAwsKotlinClientBuildService<KmsClient, AwsBuildServiceParams>() {
    override fun createClient(): KmsClient = KmsClient {
        resolveRegion()?.let { region = it }
        resolveCredentialsProvider()?.let { credentialsProvider = it }
    }
}
