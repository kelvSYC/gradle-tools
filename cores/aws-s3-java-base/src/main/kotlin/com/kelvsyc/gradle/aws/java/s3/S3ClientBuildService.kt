package com.kelvsyc.gradle.aws.java.s3

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.s3.S3Client

/**
 * Build service managing a synchronous [S3Client] instance with config-cache-safe parameters.
 *
 * Configure region and credentials via the [AwsBuildServiceParams] extension functions:
 * ```kotlin
 * gradle.sharedServices.registerIfAbsent("s3", S3ClientBuildService::class) {
 *     parameters {
 *         regionId.set("us-east-1")
 *         defaultCredentials()
 *     }
 * }
 * ```
 */
abstract class S3ClientBuildService : AbstractAwsJavaClientBuildService<S3Client, AwsBuildServiceParams>() {
    override fun createClient(): S3Client = configureBuilder(S3Client.builder()).build()
}

