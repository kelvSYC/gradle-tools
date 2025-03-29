package com.kelvsyc.gradle.aws.internal.kotlin.imds

import aws.sdk.kotlin.runtime.config.imds.EndpointConfiguration
import aws.sdk.kotlin.runtime.config.imds.ImdsClient
import aws.smithy.kotlin.runtime.client.endpoints.Endpoint
import com.kelvsyc.gradle.aws.kotlin.imds.ImdsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class ImdsClientInfoInternal : ImdsClientInfo, ServiceClientInfoInternal<ImdsClient> {
    @Suppress("LeakingThis")
    private val endpointInternal = endpoint.map { Endpoint(it) }
    private val endpointConfiguration = endpointInternal
        .map<EndpointConfiguration> { EndpointConfiguration.Custom(it) }
        .orElse(EndpointConfiguration.Default)

    override fun createClient(): ImdsClient {
        return ImdsClient {
            endpointConfiguration = this@ImdsClientInfoInternal.endpointConfiguration.get()
        }
    }
}
