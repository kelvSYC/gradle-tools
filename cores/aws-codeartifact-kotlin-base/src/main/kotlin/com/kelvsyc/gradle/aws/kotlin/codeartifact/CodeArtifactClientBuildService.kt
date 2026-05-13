package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing a [CodeartifactClient] instance.
 */
abstract class CodeArtifactClientBuildService :
    AbstractClientBuildService<CodeartifactClient, CodeArtifactClientBuildService.Params>() {
    /**
     * Configuration parameters for [CodeArtifactClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /** The AWS region that the client communicates with. */
        val region: Property<String>

        /** The credentials provider used to authenticate with AWS. */
        val credentials: Property<CredentialsProvider>
    }

    override fun createClient(): CodeartifactClient = CodeartifactClient {
        if (parameters.region.isPresent) {
            region = parameters.region.get()
        }
        if (parameters.credentials.isPresent) {
            credentialsProvider = parameters.credentials.get()
        }
    }
}
