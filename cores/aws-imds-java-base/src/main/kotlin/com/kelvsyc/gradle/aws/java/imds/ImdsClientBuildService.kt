package com.kelvsyc.gradle.aws.java.imds

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import software.amazon.awssdk.imds.Ec2MetadataClient
import software.amazon.awssdk.imds.EndpointMode
import java.net.URI

/**
 * Build service managing a synchronous [Ec2MetadataClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.endpoint] and/or [Params.endpointMode] as needed. The same registration can then be shared
 * with value sources via a `Property<ImdsClientBuildService>` parameter.
 */
abstract class ImdsClientBuildService : AbstractClientBuildService<Ec2MetadataClient, ImdsClientBuildService.Params>() {
    /**
     * Configuration parameters for [ImdsClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /** Override the IMDS endpoint URI. Leave unset for the SDK default. */
        val endpoint: Property<String>

        /** Override the IMDS endpoint mode (IPv4 or IPv6). Leave unset for the SDK default. */
        val endpointMode: Property<EndpointMode>
    }

    override fun createClient(): Ec2MetadataClient = Ec2MetadataClient.builder().apply {
        if (parameters.endpoint.isPresent) endpoint(URI.create(parameters.endpoint.get()))
        if (parameters.endpointMode.isPresent) endpointMode(parameters.endpointMode.get())
    }.build()
}
