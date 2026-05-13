package com.kelvsyc.gradle.aws.java.codeartifact

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal
import software.amazon.awssdk.services.codeartifact.model.CodeartifactException
import software.amazon.awssdk.services.codeartifact.model.EndpointType
import software.amazon.awssdk.services.codeartifact.model.GetRepositoryEndpointRequest
import software.amazon.awssdk.services.codeartifact.model.PackageFormat

/**
 * [ValueSource] implementation providing an AWS CodeArtifact repository endpoint URL.
 *
 * The value is obtained from a request to CodeArtifact.
 */
abstract class GetRepositoryEndpointValueSource : ValueSource<String, GetRepositoryEndpointValueSource.Parameters> {
    /**
     * Parameters to [GetRepositoryEndpointValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the CodeArtifact client. */
        @get:Internal
        val service: Property<CodeArtifactClientBuildService>

        /** The CodeArtifact domain name. */
        val domain: Property<String>

        /** The 12-digit account number of the domain owner. */
        val domainOwner: Property<String>

        /** The CodeArtifact repository name. */
        val repository: Property<String>

        /**
         * The endpoint type. Defaults to [EndpointType.IPV4].
         */
        val endpointType: Property<EndpointType>

        /**
         * The repository's package format. Defaults to [PackageFormat.GENERIC]
         */
        val format: Property<PackageFormat>
    }

    override fun obtain(): String? {
        val request = GetRepositoryEndpointRequest.builder().apply {
            domain(parameters.domain.get())
            domainOwner(parameters.domainOwner.get())
            repository(parameters.repository.get())
            endpointType(parameters.endpointType.getOrElse(EndpointType.IPV4))
            format(parameters.format.getOrElse(PackageFormat.GENERIC))
        }.build()

        return try {
            val response = parameters.service.get().getClient().getRepositoryEndpoint(request)
            response.repositoryEndpoint()
        } catch (_: CodeartifactException) {
            null
        }
    }
}
