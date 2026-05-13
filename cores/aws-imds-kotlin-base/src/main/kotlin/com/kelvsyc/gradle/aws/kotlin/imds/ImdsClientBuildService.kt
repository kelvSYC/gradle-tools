package com.kelvsyc.gradle.aws.kotlin.imds

import aws.sdk.kotlin.runtime.config.imds.EndpointConfiguration
import aws.sdk.kotlin.runtime.config.imds.ImdsClient
import aws.smithy.kotlin.runtime.client.endpoints.Endpoint
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing an [ImdsClient] instance.
 *
 * Unlike most AWS service build services, IMDS has no region or credentials — only an optional endpoint
 * override.
 */
abstract class ImdsClientBuildService : AbstractClientBuildService<ImdsClient, ImdsClientBuildService.Params>() {
    /**
     * Configuration parameters for [ImdsClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The IMDS endpoint URL. Leave unset to use [EndpointConfiguration.Default].
         */
        val endpoint: Property<String>
    }

    override fun createClient(): ImdsClient = ImdsClient {
        endpointConfiguration = if (parameters.endpoint.isPresent) {
            EndpointConfiguration.Custom(Endpoint(parameters.endpoint.get()))
        } else {
            EndpointConfiguration.Default
        }
    }
}
