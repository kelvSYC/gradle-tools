package com.kelvsyc.gradle.aws.java.s3

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient

/**
 * Build service managing an asynchronous [S3AsyncClient] instance built via the CRT builder.
 */
abstract class S3AsyncClientBuildService :
    AbstractClientBuildService<S3AsyncClient, S3AsyncClientBuildService.Params>() {
    /**
     * Configuration parameters for [S3AsyncClientBuildService].
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

    override fun createClient(): S3AsyncClient = S3AsyncClient.crtBuilder().apply {
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
