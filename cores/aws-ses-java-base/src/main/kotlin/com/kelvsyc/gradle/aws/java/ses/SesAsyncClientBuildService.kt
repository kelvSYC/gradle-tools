package com.kelvsyc.gradle.aws.java.ses

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.ses.SesAsyncClient

/**
 * Build service managing an asynchronous [SesAsyncClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring [AwsBuildServiceParams.regionId] and credentials as needed using the extension
 * functions on [AwsBuildServiceParams]. The same registration can then be shared with tasks
 * and work actions via a `Property<SesAsyncClientBuildService>` parameter.
 */
abstract class SesAsyncClientBuildService : AbstractAwsJavaClientBuildService<SesAsyncClient, AwsBuildServiceParams>() {
    override fun createClient(): SesAsyncClient = configureBuilder(SesAsyncClient.builder()).build()
}

