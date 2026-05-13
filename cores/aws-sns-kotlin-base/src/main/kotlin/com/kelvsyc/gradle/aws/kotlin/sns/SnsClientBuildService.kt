package com.kelvsyc.gradle.aws.kotlin.sns

import aws.sdk.kotlin.services.sns.SnsClient
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing an [SnsClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.region] and [Params.credentials] as needed. The same registration can then be shared with
 * tasks and work actions via a `Property<SnsClientBuildService>` parameter.
 */
abstract class SnsClientBuildService : AbstractClientBuildService<SnsClient, SnsClientBuildService.Params>() {
    /**
     * Configuration parameters for [SnsClientBuildService].
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

    override fun createClient(): SnsClient = SnsClient {
        if (parameters.region.isPresent) {
            region = parameters.region.get()
        }
        if (parameters.credentials.isPresent) {
            credentialsProvider = parameters.credentials.get()
        }
    }
}
