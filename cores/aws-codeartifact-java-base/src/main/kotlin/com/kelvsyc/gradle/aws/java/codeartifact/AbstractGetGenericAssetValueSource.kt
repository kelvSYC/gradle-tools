package com.kelvsyc.gradle.aws.java.codeartifact

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal
import software.amazon.awssdk.http.AbortableInputStream
import software.amazon.awssdk.services.codeartifact.model.GetPackageVersionAssetRequest
import software.amazon.awssdk.services.codeartifact.model.GetPackageVersionAssetResponse
import software.amazon.awssdk.services.codeartifact.model.PackageFormat

/**
 * Base class for [ValueSource] implementations that provide a value by reading an asset located in a CodeArtifact
 * generic repo.
 *
 * Subclasses should implement the [doObtain] function, transforming the supplied parameters to an object of the
 * desired type.
 *
 * **Configuration cache and sensitive assets:** Gradle serializes the result of every [ValueSource.obtain] call to
 * the configuration cache in plaintext when the cache is written. If the asset retrieved by a subclass contains
 * sensitive data — private keys, API tokens, credentials, or any secret material — that data will be stored in
 * `.gradle/configuration-cache/` and is readable by any process with access to the build directory. This applies
 * regardless of how the [org.gradle.api.provider.Provider] is stored: wiring the result into a task `@Input`
 * property, a `@get:Internal` property, or a private `val` all cause `obtain()` to run at configuration time and
 * the result to be cached.
 *
 * If the fetched asset may contain sensitive data, either:
 * - Use [GetGenericPackageVersionAssetAction] to download the asset to a file at task execution time, then read
 *   the file within the task action; or
 * - Call the [CodeArtifactClientBuildService] client directly inside a [org.gradle.workers.WorkAction.execute]
 *   body, where the result is never written to the cache.
 *
 * If the fetched asset is non-sensitive (version manifests, changelogs, metadata), this `ValueSource` is safe to
 * use at configuration time.
 */
abstract class AbstractGetGenericAssetValueSource<T : Any, P : AbstractGetGenericAssetValueSource.Parameters> :
    ValueSource<T, P> {
    /**
     * Base parameters interface for [AbstractGetGenericAssetValueSource]. This contains the data needed to retrieve an
     * asset from a CodeArtifact generic info.
     *
     * Extend this interface if there is a need to supply additional parameters to the
     * [AbstractGetGenericAssetValueSource] subclass.
     */
    interface Parameters : ValueSourceParameters {
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
    }

    abstract fun doObtain(response: GetPackageVersionAssetResponse, input: AbortableInputStream): T?

    override fun obtain(): T? {
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

        return parameters.service.get().getClient().getPackageVersionAsset(request, ::doObtain)
    }
}
