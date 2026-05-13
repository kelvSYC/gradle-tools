package com.kelvsyc.gradle.google.cloud.storage.fixtures

import com.kelvsyc.gradle.google.cloud.storage.StorageClientBuildService
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * Variant A baseline: a `WorkAction` whose `WorkParameters` exposes the GCS BuildService through
 * `Property<StorageClientBuildService>`. Mirrors the production shape (e.g. `UploadFileAction`).
 */
abstract class BuildServiceWorkAction : WorkAction<BuildServiceWorkAction.Parameters> {
    /** Parameters for [BuildServiceWorkAction]. */
    interface Parameters : WorkParameters {
        /** The BuildService reference under probe. */
        val service: Property<StorageClientBuildService>
    }

    override fun execute() {
        val s = parameters.service.get()
        check(s::class.qualifiedName != null) { "service class name unexpectedly null" }
    }
}
