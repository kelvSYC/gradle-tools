package com.kelvsyc.gradle.aws.java.kms

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.kms.KmsClient

/**
 * Build service managing a [KmsClient] instance.
 */
abstract class KmsClientBuildService : AbstractClientBuildService<KmsClient, KmsClientBuildService.Params>() {
    /**
     * Configuration parameters for [KmsClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /** The AWS region that the client communicates with. */
        val region: Property<Region>

        /** The credentials provider used to authenticate with AWS. */
        val credentials: Property<AwsCredentialsProvider>
    }

    override fun createClient(): KmsClient = KmsClient.builder().apply {
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
