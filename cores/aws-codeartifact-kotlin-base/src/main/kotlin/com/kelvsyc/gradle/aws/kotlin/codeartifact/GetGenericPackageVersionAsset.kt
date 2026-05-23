package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.model.GetPackageVersionAssetRequest
import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import aws.smithy.kotlin.runtime.content.writeToFile
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task implementation downloading an asset from a CodeArtifact generic repository.
 *
 * Downloads a specific asset from a CodeArtifact generic package version to a local file.
 * The output file is deterministically the same for a given package version and asset name,
 * so task output caching is enabled (not disabled).
 */
abstract class GetGenericPackageVersionAsset : DefaultTask() {

    /**
     * The build service managing the CodeArtifact client.
     * Excluded from task snapshots.
     */
    @get:Internal
    abstract val service: Property<CodeArtifactClientBuildService>

    /**
     * The CodeArtifact domain name.
     */
    @get:Input
    abstract val domain: Property<String>

    /**
     * The 12-digit account number of the domain owner.
     */
    @get:Input
    abstract val domainOwner: Property<String>

    /**
     * The CodeArtifact repository name.
     */
    @get:Input
    abstract val repository: Property<String>

    /**
     * The package namespace.
     */
    @get:Input
    abstract val namespace: Property<String>

    /**
     * The package name.
     */
    @get:Input
    abstract val packageValue: Property<String>

    /**
     * The package version.
     */
    @get:Input
    abstract val packageVersion: Property<String>

    /**
     * The asset name within the package version.
     */
    @get:Input
    abstract val asset: Property<String>

    /**
     * The location the asset is to be downloaded to.
     */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    /**
     * Downloads the asset from CodeArtifact to the specified output file.
     */
    @TaskAction
    fun execute() {
        val request = GetPackageVersionAssetRequest {
            domain = this@GetGenericPackageVersionAsset.domain.get()
            domainOwner = this@GetGenericPackageVersionAsset.domainOwner.get()
            repository = this@GetGenericPackageVersionAsset.repository.get()
            format = PackageFormat.Generic

            namespace = this@GetGenericPackageVersionAsset.namespace.get()
            `package` = this@GetGenericPackageVersionAsset.packageValue.get()
            packageVersion = this@GetGenericPackageVersionAsset.packageVersion.get()
            asset = this@GetGenericPackageVersionAsset.asset.get()
        }

        runBlocking {
            service.get().getClient().getPackageVersionAsset(request) {
                it.asset?.writeToFile(outputFile.get().asFile)
            }
        }
    }
}
