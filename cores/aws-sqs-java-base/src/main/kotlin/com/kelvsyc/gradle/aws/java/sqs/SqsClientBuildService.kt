package com.kelvsyc.gradle.aws.java.sqs

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.sqs.SqsClient

/**
 * Build service managing a synchronous [SqsClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring [AwsBuildServiceParams.regionId] and credentials as needed using the extension
 * functions on [AwsBuildServiceParams]. The same registration can then be shared with tasks
 * and work actions via a `Property<SqsClientBuildService>` parameter.
 */
abstract class SqsClientBuildService : AbstractAwsJavaClientBuildService<SqsClient, AwsBuildServiceParams>() {
    override fun createClient(): SqsClient = configureBuilder(SqsClient.builder()).build()
}

