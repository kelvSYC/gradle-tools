package com.kelvsyc.gradle.aws.java.sts

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.sts.StsClient

/**
 * Build service managing an [StsClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * parameters via the [AwsBuildServiceParams] extension functions as needed. The same registration can then
 * be shared with value sources and work actions via a `Property<StsClientBuildService>` parameter.
 */
abstract class StsClientBuildService : AbstractAwsJavaClientBuildService<StsClient, AwsBuildServiceParams>() {
    override fun createClient(): StsClient = configureBuilder(StsClient.builder()).build()
}
