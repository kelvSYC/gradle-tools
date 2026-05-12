package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.GetPackageVersionAssetRequest
import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import aws.smithy.kotlin.runtime.content.writeToFile
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.api.tasks.Internal

/**
 * [WorkAction] implementation downloading an asset form a CodeArtifact generic repo.
 */
abstract class GetGenericPackageVersionAssetAction : WorkAction<GetGenericPackageVersionAssetAction.Parameters> {
    /**
     * Parameters for [GetGenericPackageVersionAssetAction].
     */
    interface Parameters : WorkParameters {
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

        /** The package namespace. */
        val namespace: Property<String>

        /** The package name. */
        val packageValue: Property<String>

        /** The package version. */
        val packageVersion: Property<String>

        /** The asset name within the package version. */
        val asset: Property<String>

        /**
         * The location the asset is to be downloaded to.
         */
        val outputFile: RegularFileProperty
    }

    private val client: Provider<CodeartifactClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
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

        runBlocking {
            client.get().getPackageVersionAsset(request) {
                it.asset?.writeToFile(parameters.outputFile.get().asFile)
            }
        }
    }
}
