package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.GetPackageVersionAssetRequest
import aws.sdk.kotlin.services.codeartifact.model.GetPackageVersionAssetResponse
import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * Base class for [ValueSource] implementations that provide a value by reading an asset located in a CodeArtifact
 * generic repo.
 *
 * Subclasses should implement the [doObtain] function, transforming the supplied parameters to an object of the
 * desired type.
 */
abstract class AbstractGetGenericAssetValueSource<T, P : AbstractGetGenericAssetValueSource.Parameters> : ValueSource<T, P> {
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

    abstract fun doObtain(response: GetPackageVersionAssetResponse): T?

    override fun obtain(): T? {
        val request = GetPackageVersionAssetRequest {
            domain = parameters.domain.get()
            domainOwner = parameters.domainOwner.get()
            repository = parameters.repository.get()
            format = PackageFormat.Generic

            namespace = parameters.namespace.get()
            `package` = parameters.packageValue.get()
            packageVersion = parameters.packageVersion.get()
            asset = parameters.asset.get()
        }

        return runBlocking {
            client.get().getPackageVersionAsset(request, ::doObtain)
        }
    }
}
