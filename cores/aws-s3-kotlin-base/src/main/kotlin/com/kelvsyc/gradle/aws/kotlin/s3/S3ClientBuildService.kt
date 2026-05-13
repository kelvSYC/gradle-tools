package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing an [S3Client] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.region] and [Params.credentials] as needed. The same registration can then be shared with
 * tasks, work actions and value sources via a `Property<S3ClientBuildService>` parameter.
 */
abstract class S3ClientBuildService : AbstractClientBuildService<S3Client, S3ClientBuildService.Params>() {
    /**
     * Configuration parameters for [S3ClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The AWS region that the client communicates with.
         *
         * Leave unset to identify the region using the default region provider chain of the AWS SDK for Kotlin.
         */
        val region: Property<String>

        /**
         * The credentials provider used to authenticate with AWS.
         *
         * Leave unset to identify credentials using the default credentials provider chain of the AWS SDK for Kotlin.
         */
        val credentials: Property<CredentialsProvider>
    }

    override fun createClient(): S3Client = S3Client {
        if (parameters.region.isPresent) {
            region = parameters.region.get()
        }
        if (parameters.credentials.isPresent) {
            credentialsProvider = parameters.credentials.get()
        }
    }
}
