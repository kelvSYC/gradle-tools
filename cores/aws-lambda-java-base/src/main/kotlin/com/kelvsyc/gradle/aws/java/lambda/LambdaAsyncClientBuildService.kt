package com.kelvsyc.gradle.aws.java.lambda

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.lambda.LambdaAsyncClient

/**
 * Build service managing an asynchronous [LambdaAsyncClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * parameters via the [AwsBuildServiceParams] extension functions as needed. The same registration can then
 * be shared with tasks via a `Property<LambdaAsyncClientBuildService>` parameter.
 *
 * Use this service with [AsyncBatchUpdateFunctionCode] for concurrent Lambda code updates via
 * `CompletableFuture`. For synchronous updates, use [LambdaClientBuildService] instead.
 *
 * @see LambdaClientBuildService
 */
abstract class LambdaAsyncClientBuildService :
    AbstractAwsJavaClientBuildService<LambdaAsyncClient, AwsBuildServiceParams>() {
    override fun createClient(): LambdaAsyncClient = configureBuilder(LambdaAsyncClient.builder()).build()
}
