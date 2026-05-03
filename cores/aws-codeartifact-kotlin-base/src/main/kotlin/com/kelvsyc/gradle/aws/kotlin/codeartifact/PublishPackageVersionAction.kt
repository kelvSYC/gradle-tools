package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import aws.sdk.kotlin.services.codeartifact.model.PublishPackageVersionRequest
import aws.smithy.kotlin.runtime.content.asByteStream
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation publishing an asset to a CodeArtifact generic package version.
 */
abstract class PublishPackageVersionAction : WorkAction<PublishPackageVersionAction.Parameters> {
    /**
     * Parameters for [PublishPackageVersionAction].
     */
    interface Parameters : WorkParameters {
        /** The shared build service managing CodeArtifact clients. */
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
        val request = PublishPackageVersionRequest {
            domain = parameters.domain.get()
            domainOwner = parameters.domainOwner.get()
            repository = parameters.repository.get()
            format = PackageFormat.Generic

            namespace = parameters.namespace.get()
            `package` = parameters.packageValue.get()
            packageVersion = parameters.packageVersion.get()
            assetName = parameters.assetName.get()
            assetSha256 = parameters.assetSHA256.get()
            assetContent = parameters.assetContent.get().asFile.asByteStream()

            if (parameters.unfinished.isPresent) {
                unfinished = parameters.unfinished.get()
            }
        }

        runBlocking {
            client.get().publishPackageVersion(request)
        }
    }
}
