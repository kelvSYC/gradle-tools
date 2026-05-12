package com.kelvsyc.gradle.aws.java.codeartifact

import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.providers.asPath
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.codeartifact.CodeartifactClient
import software.amazon.awssdk.services.codeartifact.model.PackageFormat
import software.amazon.awssdk.services.codeartifact.model.PublishPackageVersionRequest
import org.gradle.api.tasks.Internal

/**
 * [WorkAction] implementation publishing an asset to a CodeArtifact generic package version.
 */
abstract class PublishPackageVersionAction : WorkAction<PublishPackageVersionAction.Parameters> {
    /**
     * Parameters for [PublishPackageVersionAction].
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
        val assetName: Property<String>

        /** The SHA-256 hash of the asset content. */
        val assetSHA256: Property<String>

        /** The asset file to upload. */
        val assetContent: RegularFileProperty

        /**
         * Whether the package version should remain in the `Unfinished` state after publishing.
         *
         * Set to `true` when uploading multiple assets to the same package version.
         */
        val unfinished: Property<Boolean>
    }

    private val client: Provider<CodeartifactClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val request = PublishPackageVersionRequest.builder().apply {
            domain(parameters.domain.get())
            domainOwner(parameters.domainOwner.get())
            repository(parameters.repository.get())
            format(PackageFormat.GENERIC)

            namespace(parameters.namespace.get())
            packageValue(parameters.packageValue.get())
            packageVersion(parameters.packageVersion.get())
            assetName(parameters.assetName.get())
            assetSHA256(parameters.assetSHA256.get())

            if (parameters.unfinished.isPresent) {
                unfinished(parameters.unfinished.get())
            }
        }.build()

        client.get().publishPackageVersion(request, parameters.assetContent.asPath.get())
    }
}
