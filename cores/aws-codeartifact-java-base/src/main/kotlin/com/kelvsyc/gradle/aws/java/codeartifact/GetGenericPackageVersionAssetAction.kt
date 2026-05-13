package com.kelvsyc.gradle.aws.java.codeartifact

import com.kelvsyc.gradle.providers.asPath
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
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
        /** The build service managing the CodeArtifact client. */
        @get:Internal
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

        parameters.service.get().getClient().getPackageVersionAsset(request, parameters.outputFile.asPath.get())
    }
}
