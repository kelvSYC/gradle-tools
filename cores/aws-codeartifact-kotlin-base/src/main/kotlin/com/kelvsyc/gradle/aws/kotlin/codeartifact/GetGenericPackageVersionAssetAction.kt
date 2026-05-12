package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.model.GetPackageVersionAssetRequest
import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import aws.smithy.kotlin.runtime.content.writeToFile
import kotlinx.coroutines.runBlocking
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation downloading an asset from a CodeArtifact generic repo.
 */
abstract class GetGenericPackageVersionAssetAction : WorkAction<GetGenericPackageVersionAssetAction.Parameters> {
    /**
     * Parameters for [GetGenericPackageVersionAssetAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the CodeArtifact client. */
        val service: Property<CodeArtifactClientBuildService>

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
            parameters.service.get().getClient().getPackageVersionAsset(request) {
                it.asset?.writeToFile(parameters.outputFile.get().asFile)
            }
        }
    }
}
