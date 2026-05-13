package com.kelvsyc.gradle.aws.java.ses

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.SesClient

/**
 * Build service managing a synchronous [SesClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.region] and [Params.credentials] as needed. The same registration can then be shared with
 * tasks and work actions via a `Property<SesClientBuildService>` parameter.
 */
abstract class SesClientBuildService : AbstractClientBuildService<SesClient, SesClientBuildService.Params>() {
    /**
     * Configuration parameters for [SesClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The AWS region that the client communicates with.
         *
         * Leave unset to fall back to
         * [DefaultAwsRegionProviderChain][software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain].
         */
        val region: Property<Region>

        /**
         * The credentials provider used to authenticate with AWS.
         *
         * If unset, the client uses [AnonymousCredentialsProvider].
         */
        val credentials: Property<AwsCredentialsProvider>
    }

    override fun createClient(): SesClient = SesClient.builder().apply {
        if (parameters.region.isPresent) {
            region(parameters.region.get())
        }
        if (parameters.credentials.isPresent) {
            credentialsProvider(parameters.credentials.get())
        } else {
            credentialsProvider(AnonymousCredentialsProvider.create())
        }
    }.build()
}
