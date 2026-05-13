package com.kelvsyc.gradle.azure.storage.blob.fixtures

import com.kelvsyc.gradle.azure.storage.blob.BlobServiceClientBuildService
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * Variant A baseline: a `WorkAction` whose `WorkParameters` exposes the Azure Blob BuildService through
 * `Property<BlobServiceClientBuildService>`. Mirrors the production shape (e.g. `UploadBlobAction`).
 */
abstract class BuildServiceWorkAction : WorkAction<BuildServiceWorkAction.Parameters> {
    /** Parameters for [BuildServiceWorkAction]. */
    interface Parameters : WorkParameters {
        /** The BuildService reference under probe. */
        val service: Property<BlobServiceClientBuildService>
    }

    override fun execute() {
        val s = parameters.service.get()
        check(s::class.qualifiedName != null) { "service class name unexpectedly null" }
    }
}
