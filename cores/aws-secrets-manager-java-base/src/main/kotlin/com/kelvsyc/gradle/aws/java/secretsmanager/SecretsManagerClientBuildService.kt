package com.kelvsyc.gradle.aws.java.secretsmanager

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient

/**
 * Build service managing a synchronous [SecretsManagerClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.region] and [Params.credentials] as needed.
 */
abstract class SecretsManagerClientBuildService :
    AbstractClientBuildService<SecretsManagerClient, SecretsManagerClientBuildService.Params>() {
    /**
     * Configuration parameters for [SecretsManagerClientBuildService].
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

    override fun createClient(): SecretsManagerClient = SecretsManagerClient.builder().apply {
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
