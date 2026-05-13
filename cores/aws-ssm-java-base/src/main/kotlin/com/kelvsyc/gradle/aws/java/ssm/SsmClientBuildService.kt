package com.kelvsyc.gradle.aws.java.ssm

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ssm.SsmClient

/**
 * Build service managing an [SsmClient] instance.
 */
abstract class SsmClientBuildService : AbstractClientBuildService<SsmClient, SsmClientBuildService.Params>() {
    /**
     * Configuration parameters for [SsmClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /** The AWS region that the client communicates with. */
        val region: Property<Region>

        /** The credentials provider used to authenticate with AWS. */
        val credentials: Property<AwsCredentialsProvider>
    }

    override fun createClient(): SsmClient = SsmClient.builder().apply {
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
