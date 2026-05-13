package com.kelvsyc.gradle.aws.java.sns

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.sns.SnsAsyncClient

/**
 * Build service managing an asynchronous [SnsAsyncClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring [AwsBuildServiceParams.regionId] and credentials as needed using the extension
 * functions on [AwsBuildServiceParams]. The same registration can then be shared with value
 * sources and work actions via a `Property<SnsAsyncClientBuildService>` parameter.
 */
abstract class SnsAsyncClientBuildService : AbstractAwsJavaClientBuildService<SnsAsyncClient, AwsBuildServiceParams>() {
    override fun createClient(): SnsAsyncClient = configureBuilder(SnsAsyncClient.builder()).build()
}

