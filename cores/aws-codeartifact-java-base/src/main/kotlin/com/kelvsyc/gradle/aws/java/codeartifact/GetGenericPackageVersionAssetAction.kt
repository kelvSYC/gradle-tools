package com.kelvsyc.gradle.aws.java.codeartifact

import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.providers.asPath
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.codeartifact.CodeartifactClient
import software.amazon.awssdk.services.codeartifact.model.GetPackageVersionAssetRequest
import software.amazon.awssdk.services.codeartifact.model.PackageFormat

/**
 * [WorkAction] implementation downloading an asset form a CodeArtifact generic repo.
 */
abstract class GetGenericPackageVersionAssetAction : WorkAction<GetGenericPackageVersionAssetAction.Parameters> {
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

        client.get().getPackageVersionAsset(request, parameters.outputFile.asPath.get())
    }
}
