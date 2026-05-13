package com.kelvsyc.gradle.aws.java.s3

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.s3.S3AsyncClient

/**
 * Build service managing an asynchronous [S3AsyncClient] instance built via the CRT builder,
 * with config-cache-safe parameters.
 *
 * Configure region and credentials via the [AwsBuildServiceParams] extension functions:
 * ```kotlin
 * gradle.sharedServices.registerIfAbsent("s3-async", S3AsyncClientBuildService::class) {
 *     parameters {
 *         regionId.set("us-east-1")
 *         defaultCredentials()
 *     }
 * }
 * ```
 */
abstract class S3AsyncClientBuildService : AbstractAwsJavaClientBuildService<S3AsyncClient, AwsBuildServiceParams>() {
    override fun createClient(): S3AsyncClient = S3AsyncClient.crtBuilder().apply {
        resolveRegion()?.let { region(it) }
        credentialsProvider(resolveCredentialsProvider())
    }.build()
}

