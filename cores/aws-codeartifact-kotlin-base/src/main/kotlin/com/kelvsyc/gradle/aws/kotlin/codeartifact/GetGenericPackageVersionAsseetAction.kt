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

/**
 * [WorkAction] implementation downloading an asset form a CodeArtifact generic repo.
 */
abstract class GetGenericPackageVersionAsseetAction : WorkAction<GetGenericPackageVersionAsseetAction.Parameters> {
    /**
     * Parameters for [GetGenericPackageVersionAssetAction].
     */
    interface Parameters : WorkParameters {
        val service: Property<ClientsBaseService>
        val clientName: Property<String>

        val domain: Property<String>
        val domainOwner: Property<String>
        val repository: Property<String>

        val namespace: Property<String>
        val packageValue: Property<String>
        val packageVersion: Property<String>
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
