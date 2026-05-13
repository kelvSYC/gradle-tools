package com.kelvsyc.gradle.aws.java.ecr

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ecr.EcrClient

/**
 * Build service managing a synchronous [EcrClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.region] and [Params.credentials] as needed. The same registration can then be shared with
 * value sources and work actions via a `Property<EcrClientBuildService>` parameter.
 */
abstract class EcrClientBuildService : AbstractClientBuildService<EcrClient, EcrClientBuildService.Params>() {
    /**
     * Configuration parameters for [EcrClientBuildService].
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

    override fun createClient(): EcrClient = EcrClient.builder().apply {
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
