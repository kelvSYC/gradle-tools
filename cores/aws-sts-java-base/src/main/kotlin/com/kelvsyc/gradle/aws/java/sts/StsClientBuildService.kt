package com.kelvsyc.gradle.aws.java.sts

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sts.StsClient

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
         * Leave unset to identify the region using
         * [DefaultAwsRegionProviderChain][software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain],
         * the default used by the AWS SDK for Java.
         */
        val region: Property<Region>

        /**
         * The credentials provider used to authenticate with AWS.
         *
         * If unset, the client uses [AnonymousCredentialsProvider]. Set to
         * [DefaultCredentialsProvider][software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider]
         * to use the default credentials chain.
         */
        val credentials: Property<AwsCredentialsProvider>
    }

    override fun createClient(): StsClient = StsClient.builder().apply {
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
