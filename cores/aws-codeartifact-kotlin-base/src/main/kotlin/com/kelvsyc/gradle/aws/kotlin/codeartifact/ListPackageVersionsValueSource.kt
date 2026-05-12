package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.ListPackageVersionsRequest
import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

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
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of a [CodeArtifactClientInfo]. */
        val clientName: Property<String>

        /** The CodeArtifact domain name. */
        val domain: Property<String>

        /** The 12-digit account number of the domain owner. */
        val domainOwner: Property<String>

        /** The CodeArtifact repository name. */
        val repository: Property<String>

        /** The package format as a string value. */
        val format: Property<String>

        /** The package namespace. */
        val namespace: Property<String>

        /** The package name. */
        val packageValue: Property<String>
    }

    private val client: Provider<CodeartifactClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): List<String>? {
        val request = ListPackageVersionsRequest {
            domain = parameters.domain.get()
            domainOwner = parameters.domainOwner.get()
            repository = parameters.repository.get()
            format = PackageFormat.fromValue(parameters.format.get())
            namespace = parameters.namespace.get()
            `package` = parameters.packageValue.get()
        }

        return runBlocking {
            val versions = mutableListOf<String>()
            var nextToken: String? = null
            do {
                val pageRequest = request.copy { this.nextToken = nextToken }
                val response = client.get().listPackageVersions(pageRequest)
                response.versions?.forEach { summary -> summary.version?.let { versions.add(it) } }
                nextToken = response.nextToken
            } while (nextToken != null)
            versions
        }
    }
}
