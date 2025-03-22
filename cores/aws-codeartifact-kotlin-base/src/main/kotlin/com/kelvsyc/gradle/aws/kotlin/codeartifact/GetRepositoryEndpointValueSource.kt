package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.EndpointType
import aws.sdk.kotlin.services.codeartifact.model.GetRepositoryEndpointRequest
import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * [ValueSource] implementation providing an AWS CodeArtifact repository endpoint URL.
 *
 * The value is obtained from a request to CodeArtifact.
 */
abstract class GetRepositoryEndpointValueSource : ValueSource<String, GetRepositoryEndpointValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        val service: Property<ClientsBaseService>
        val clientName: Property<String>

        val domain: Property<String>
        val domainOwner: Property<String>
        val repository: Property<String>

        /**
         * The endpoint type. Defaults to [EndpointType.Ipv4].
         */
        val endpointType: Property<EndpointType>

        /**
         * The repository's package format. Defaults to [PackageFormat.Generic]
         */
        val format: Property<PackageFormat>
    }

    private val client: Provider<CodeartifactClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): String? {
        val request = GetRepositoryEndpointRequest {
            domain = parameters.domain.get()
            domainOwner = parameters.domainOwner.get()
            repository = parameters.repository.get()

            endpointType = parameters.endpointType.getOrElse(EndpointType.Ipv4)
            format = parameters.format.getOrElse(PackageFormat.Generic)
        }

        return runBlocking {
            client.get().getRepositoryEndpoint(request).repositoryEndpoint
        }
    }
}
