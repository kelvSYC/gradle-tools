package com.kelvsyc.gradle.aws.java.ecr

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.ecr.EcrClient

/**
 * Build service managing a synchronous [EcrClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * parameters via the [AwsBuildServiceParams] extension functions as needed. The same registration can then
 * be shared with value sources and work actions via a `Property<EcrClientBuildService>` parameter.
 */
abstract class EcrClientBuildService : AbstractAwsJavaClientBuildService<EcrClient, AwsBuildServiceParams>() {
    override fun createClient(): EcrClient = configureBuilder(EcrClient.builder()).build()
}
