package com.kelvsyc.gradle.aws.java.ses

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.ses.SesClient

/**
 * Build service managing a synchronous [SesClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring [AwsBuildServiceParams.regionId] and credentials as needed using the extension
 * functions on [AwsBuildServiceParams]. The same registration can then be shared with tasks
 * and work actions via a `Property<SesClientBuildService>` parameter.
 */
abstract class SesClientBuildService : AbstractAwsJavaClientBuildService<SesClient, AwsBuildServiceParams>() {
    override fun createClient(): SesClient = configureBuilder(SesClient.builder()).build()
}

