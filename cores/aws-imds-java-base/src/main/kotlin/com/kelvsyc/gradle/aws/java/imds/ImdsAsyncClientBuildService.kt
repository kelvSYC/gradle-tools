package com.kelvsyc.gradle.aws.java.imds

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import software.amazon.awssdk.imds.Ec2MetadataAsyncClient
import software.amazon.awssdk.imds.EndpointMode
import java.net.URI

/**
 * Build service managing an asynchronous [Ec2MetadataAsyncClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.endpoint] and/or [Params.endpointMode] as needed. The same registration can then be shared
 * with value sources via a `Property<ImdsAsyncClientBuildService>` parameter.
 */
abstract class ImdsAsyncClientBuildService :
    AbstractClientBuildService<Ec2MetadataAsyncClient, ImdsAsyncClientBuildService.Params>() {
    /**
     * Configuration parameters for [ImdsAsyncClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /** Override the IMDS endpoint URI. Leave unset for the SDK default. */
        val endpoint: Property<String>

        /** Override the IMDS endpoint mode (IPv4 or IPv6). Leave unset for the SDK default. */
        val endpointMode: Property<EndpointMode>
    }

    override fun createClient(): Ec2MetadataAsyncClient = Ec2MetadataAsyncClient.builder().apply {
        if (parameters.endpoint.isPresent) endpoint(URI.create(parameters.endpoint.get()))
        if (parameters.endpointMode.isPresent) endpointMode(parameters.endpointMode.get())
    }.build()
}
