package com.kelvsyc.gradle.aws.kotlin.ssm

import aws.sdk.kotlin.services.ssm.SsmClient
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing an [SsmClient] instance.
 */
abstract class SsmClientBuildService : AbstractClientBuildService<SsmClient, SsmClientBuildService.Params>() {
    /**
     * Configuration parameters for [SsmClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /** The AWS region that the client communicates with. */
        val region: Property<String>

        /** The credentials provider used to authenticate with AWS. */
        val credentials: Property<CredentialsProvider>
    }

    override fun createClient(): SsmClient = SsmClient {
        if (parameters.region.isPresent) {
            region = parameters.region.get()
        }
        if (parameters.credentials.isPresent) {
            credentialsProvider = parameters.credentials.get()
        }
    }
}
