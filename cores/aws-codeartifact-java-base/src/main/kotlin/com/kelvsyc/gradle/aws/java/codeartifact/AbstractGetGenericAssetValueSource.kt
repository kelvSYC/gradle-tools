package com.kelvsyc.gradle.aws.java.codeartifact

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.http.AbortableInputStream
import software.amazon.awssdk.services.codeartifact.CodeartifactClient
import software.amazon.awssdk.services.codeartifact.model.GetPackageVersionAssetRequest
import software.amazon.awssdk.services.codeartifact.model.GetPackageVersionAssetResponse
import software.amazon.awssdk.services.codeartifact.model.PackageFormat

/**
 * Base class for [ValueSource] implementations that provide a value by reading an asset located in a CodeArtifact
 * generic repo.
 *
 * Subclasses should implement the [doObtain] function, transforming the supplied parameters to an object of the
 * desired type.
 */
abstract class AbstractGetGenericAssetValueSource<T, P : AbstractGetGenericAssetValueSource.Parameters> :
    ValueSource<T, P> {
    /**
     * Base parameters interface for [AbstractGetGenericAssetValueSource]. This contains the data needed to retrieve an
     * asset from a CodeArtifact generic info.
     *
     * Extend this interface if there is a need to supply additional parameters to the
     * [AbstractGetGenericAssetValueSource] subclass.
     */
    interface Parameters : ValueSourceParameters {
        val service: Property<ClientsBaseService>
        val clientName: Property<String>

        val domain: Property<String>
        val domainOwner: Property<String>
        val repository: Property<String>

        val namespace: Property<String>
        val packageValue: Property<String>
        val packageVersion: Property<String>
        val asset: Property<String>
    }

    private val client: Provider<CodeartifactClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    abstract fun doObtain(response: GetPackageVersionAssetResponse, input: AbortableInputStream): T?

    override fun obtain(): T? {
        val request = GetPackageVersionAssetRequest.builder().apply {
            domain(parameters.domain.get())
            domainOwner(parameters.domainOwner.get())
            repository(parameters.repository.get())
            format(PackageFormat.GENERIC)

            namespace(parameters.namespace.get())
            packageValue(parameters.packageValue.get())
            packageVersion(parameters.packageVersion.get())
            asset(parameters.asset.get())
        }.build()

        return client.get().getPackageVersionAsset(request, ::doObtain)
    }
}
