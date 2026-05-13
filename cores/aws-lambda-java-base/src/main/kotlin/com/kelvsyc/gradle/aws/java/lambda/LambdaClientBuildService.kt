package com.kelvsyc.gradle.aws.java.lambda

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.lambda.LambdaClient

/**
 * Build service managing a synchronous [LambdaClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * parameters via the [AwsBuildServiceParams] extension functions as needed. The same registration can then
 * be shared with value sources and work actions via a `Property<LambdaClientBuildService>` parameter.
 */
abstract class LambdaClientBuildService : AbstractAwsJavaClientBuildService<LambdaClient, AwsBuildServiceParams>() {
    override fun createClient(): LambdaClient = configureBuilder(LambdaClient.builder()).build()
}
