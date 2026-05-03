package com.kelvsyc.gradle.aws.java.codeartifact

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.services.codeartifact.CodeartifactClient
import software.amazon.awssdk.services.codeartifact.model.CodeartifactException
import software.amazon.awssdk.services.codeartifact.model.ListPackageVersionsRequest
import software.amazon.awssdk.services.codeartifact.model.ListPackageVersionsResponse
import software.amazon.awssdk.services.codeartifact.model.PackageFormat

/**
 * [ValueSource] implementation providing a list of package version strings from a CodeArtifact repository.
 *
 * The value is obtained by paginating through the CodeArtifact ListPackageVersions API.
 */
abstract class ListPackageVersionsValueSource :
    ValueSource<List<String>, ListPackageVersionsValueSource.Parameters> {
    /**
     * Parameters for [ListPackageVersionsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing CodeArtifact clients. */
        val service: Property<ClientsBaseService>

        /** Registered name of a [CodeArtifactClientInfo]. */
        val clientName: Property<String>

        /** The CodeArtifact domain name. */
        val domain: Property<String>

        /** The 12-digit account number of the domain owner. */
        val domainOwner: Property<String>

        /** The CodeArtifact repository name. */
        val repository: Property<String>

        /** The package format. */
        val format: Property<PackageFormat>

        /** The package namespace. */
        val namespace: Property<String>

        /** The package name. */
        val packageValue: Property<String>
    }

    private val client: Provider<CodeartifactClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): List<String>? {
        val baseRequest = ListPackageVersionsRequest.builder().apply {
            domain(parameters.domain.get())
            domainOwner(parameters.domainOwner.get())
            repository(parameters.repository.get())
            format(parameters.format.get())
            namespace(parameters.namespace.get())
            packageValue(parameters.packageValue.get())
        }

        return try {
            val versions = mutableListOf<String>()
            var nextToken: String? = null
            do {
                val request = baseRequest.nextToken(nextToken).build()
                val response: ListPackageVersionsResponse = client.get().listPackageVersions(request)
                response.versions().forEach { versions.add(it.version()) }
                nextToken = response.nextToken()
            } while (nextToken != null)
            versions
        } catch (_: CodeartifactException) {
            null
        }
    }
}
