package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.model.EndpointType
import aws.sdk.kotlin.services.codeartifact.model.GetRepositoryEndpointRequest
import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * [ValueSource] implementation providing an AWS CodeArtifact repository endpoint URL.
 *
 * The value is obtained from a request to CodeArtifact.
 */
abstract class GetRepositoryEndpointValueSource : ValueSource<String, GetRepositoryEndpointValueSource.Parameters> {
    /**
     * Parameters for [GetRepositoryEndpointValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the CodeArtifact client. */
        val service: Property<CodeArtifactClientBuildService>

        /** The CodeArtifact domain name. */
        val domain: Property<String>

        /** The 12-digit account number of the domain owner. */
        val domainOwner: Property<String>

        /** The CodeArtifact repository name. */
        val repository: Property<String>

        /**
         * The endpoint type as a string value. Defaults to [EndpointType.Ipv4].
         *
         * @see [EndpointType.value]
         */
        val endpointType: Property<String>

        /**
         * The repository's package format as a string value. Defaults to [PackageFormat.Generic].
         *
         * @see [PackageFormat.value]
         */
        val format: Property<String>
    }

    override fun obtain(): String? {
        val request = GetRepositoryEndpointRequest {
            domain = parameters.domain.get()
            domainOwner = parameters.domainOwner.get()
            repository = parameters.repository.get()

            endpointType = parameters.endpointType.map { EndpointType.fromValue(it) }.getOrElse(EndpointType.Ipv4)
            format = parameters.format.map { PackageFormat.fromValue(it) }.getOrElse(PackageFormat.Generic)
        }

        return runBlocking {
            parameters.service.get().getClient().getRepositoryEndpoint(request).repositoryEndpoint
        }
    }
}
