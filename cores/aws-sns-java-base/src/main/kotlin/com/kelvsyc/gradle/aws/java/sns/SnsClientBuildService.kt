package com.kelvsyc.gradle.aws.java.sns

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.sns.SnsClient

/**
 * Build service managing a synchronous [SnsClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring [AwsBuildServiceParams.regionId] and credentials as needed using the extension
 * functions on [AwsBuildServiceParams]. The same registration can then be shared with value
 * sources and work actions via a `Property<SnsClientBuildService>` parameter.
 */
abstract class SnsClientBuildService : AbstractAwsJavaClientBuildService<SnsClient, AwsBuildServiceParams>() {
    override fun createClient(): SnsClient = configureBuilder(SnsClient.builder()).build()
}

