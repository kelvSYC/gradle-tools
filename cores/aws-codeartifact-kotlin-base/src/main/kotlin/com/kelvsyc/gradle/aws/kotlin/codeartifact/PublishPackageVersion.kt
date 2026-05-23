package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import aws.sdk.kotlin.services.codeartifact.model.PublishPackageVersionRequest
import aws.smithy.kotlin.runtime.content.asByteStream
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Task implementation publishing an asset to a CodeArtifact generic package version.
 *
 * Publishes an asset (file) to a CodeArtifact generic package version. The asset file is
 * hashed and uploaded with metadata identifying the package and version it belongs to.
 */
@UntrackedTask(because = "Communicates with AWS CodeArtifact; no local output")
abstract class PublishPackageVersion : DefaultTask() {

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
    abstract val assetName: Property<String>

    /**
     * The SHA-256 hash of the asset content.
     */
    @get:Input
    abstract val assetSHA256: Property<String>

    /**
     * The asset file to upload.
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val assetContent: RegularFileProperty

    /**
     * Whether the package version should remain in the `Unfinished` state after publishing.
     *
     * Set to `true` when uploading multiple assets to the same package version. When `false` or
     * unset, the package version is marked as finished after the asset is published.
     */
    @get:Input
    @get:Optional
    abstract val unfinished: Property<Boolean>

    /**
     * Publishes the asset to CodeArtifact.
     */
    @TaskAction
    fun execute() {
        val request = PublishPackageVersionRequest {
            domain = this@PublishPackageVersion.domain.get()
            domainOwner = this@PublishPackageVersion.domainOwner.get()
            repository = this@PublishPackageVersion.repository.get()
            format = PackageFormat.Generic

            namespace = this@PublishPackageVersion.namespace.get()
            `package` = this@PublishPackageVersion.packageValue.get()
            packageVersion = this@PublishPackageVersion.packageVersion.get()
            assetName = this@PublishPackageVersion.assetName.get()
            assetSha256 = this@PublishPackageVersion.assetSHA256.get()
            assetContent = this@PublishPackageVersion.assetContent.get().asFile.asByteStream()

            if (this@PublishPackageVersion.unfinished.isPresent) {
                unfinished = this@PublishPackageVersion.unfinished.get()
            }
        }

        runBlocking {
            service.get().getClient().publishPackageVersion(request)
        }
    }
}
