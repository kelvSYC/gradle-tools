package com.kelvsyc.gradle.aws.kotlin.sts

import aws.sdk.kotlin.services.sts.StsClient
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing an [StsClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.region] and [Params.credentials] as needed. The same registration can then be shared with
 * value sources and work actions via a `Property<StsClientBuildService>` parameter.
 */
abstract class StsClientBuildService : AbstractClientBuildService<StsClient, StsClientBuildService.Params>() {
    /**
     * Configuration parameters for [StsClientBuildService].
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

    override fun createClient(): StsClient = StsClient {
        if (parameters.region.isPresent) {
            region = parameters.region.get()
        }
        if (parameters.credentials.isPresent) {
            credentialsProvider = parameters.credentials.get()
        }
    }
}
