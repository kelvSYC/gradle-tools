package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.EcrClient
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing an [EcrClient] instance.
 */
abstract class EcrClientBuildService : AbstractClientBuildService<EcrClient, EcrClientBuildService.Params>() {
    /**
     * Configuration parameters for [EcrClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /** The AWS region that the client communicates with. */
        val region: Property<String>

        /** The credentials provider used to authenticate with AWS. */
        val credentials: Property<CredentialsProvider>
    }

    override fun createClient(): EcrClient = EcrClient {
        if (parameters.region.isPresent) {
            region = parameters.region.get()
        }
        if (parameters.credentials.isPresent) {
            credentialsProvider = parameters.credentials.get()
        }
    }
}
