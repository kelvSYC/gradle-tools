package com.kelvsyc.gradle.aws.java.sqs

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.sqs.SqsAsyncClient

/**
 * Build service managing an asynchronous [SqsAsyncClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring [AwsBuildServiceParams.regionId] and credentials as needed using the extension
 * functions on [AwsBuildServiceParams]. The same registration can then be shared with tasks
 * and work actions via a `Property<SqsAsyncClientBuildService>` parameter.
 */
abstract class SqsAsyncClientBuildService : AbstractAwsJavaClientBuildService<SqsAsyncClient, AwsBuildServiceParams>() {
    override fun createClient(): SqsAsyncClient = configureBuilder(SqsAsyncClient.builder()).build()
}

