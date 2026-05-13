package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing a [LambdaClient] instance.
 */
abstract class LambdaClientBuildService : AbstractClientBuildService<LambdaClient, LambdaClientBuildService.Params>() {
    /**
     * Configuration parameters for [LambdaClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /** The AWS region that the client communicates with. */
        val region: Property<String>

        /** The credentials provider used to authenticate with AWS. */
        val credentials: Property<CredentialsProvider>
    }

    override fun createClient(): LambdaClient = LambdaClient {
        if (parameters.region.isPresent) {
            region = parameters.region.get()
        }
        if (parameters.credentials.isPresent) {
            credentialsProvider = parameters.credentials.get()
        }
    }
}
