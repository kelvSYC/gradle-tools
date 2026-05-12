package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.model.ListPackageVersionsRequest
import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

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
        /** The build service managing the CodeArtifact client. */
        val service: Property<CodeArtifactClientBuildService>

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

    override fun obtain(): List<String>? {
        val request = ListPackageVersionsRequest {
            domain = parameters.domain.get()
            domainOwner = parameters.domainOwner.get()
            repository = parameters.repository.get()
            format = PackageFormat.fromValue(parameters.format.get())
            namespace = parameters.namespace.get()
            `package` = parameters.packageValue.get()
        }
        val client = parameters.service.get().getClient()

        return runBlocking {
            val versions = mutableListOf<String>()
            var nextToken: String? = null
            do {
                val pageRequest = request.copy { this.nextToken = nextToken }
                val response = client.listPackageVersions(pageRequest)
                response.versions?.forEach { summary -> summary.version?.let { versions.add(it) } }
                nextToken = response.nextToken
            } while (nextToken != null)
            versions
        }
    }
}
