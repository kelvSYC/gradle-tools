package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing a [SecretsManagerClient] instance.
 */
abstract class SecretsManagerClientBuildService :
    AbstractClientBuildService<SecretsManagerClient, SecretsManagerClientBuildService.Params>() {
    /**
     * Configuration parameters for [SecretsManagerClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /** The AWS region that the client communicates with. */
        val region: Property<String>

        /** The credentials provider used to authenticate with AWS. */
        val credentials: Property<CredentialsProvider>
    }

    override fun createClient(): SecretsManagerClient = SecretsManagerClient {
        if (parameters.region.isPresent) {
            region = parameters.region.get()
        }
        if (parameters.credentials.isPresent) {
            credentialsProvider = parameters.credentials.get()
        }
    }
}
