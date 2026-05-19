package com.kelvsyc.gradle.google.cloud.functions

import com.google.cloud.functions.v2.GetFunctionRequest
import com.google.cloud.functions.v2.Source
import com.google.cloud.functions.v2.StorageSource
import com.google.cloud.functions.v2.UpdateFunctionRequest
import com.google.protobuf.FieldMask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that updates the source of a Cloud Functions Gen 2 function to a
 * zip file already staged in Google Cloud Storage.
 *
 * The caller is responsible for uploading the zip to GCS beforehand (e.g. using
 * `google-cloud-storage-base`'s `UploadFileAction`). The function update is submitted as a
 * long-running operation and this action blocks until it completes.
 *
 * To upload a local zip and update in a single step, use [UploadAndUpdateFunctionAction] instead.
 */
abstract class UpdateFunctionAction : WorkAction<UpdateFunctionAction.Parameters> {

    /**
     * Parameters for [UpdateFunctionAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the Cloud Functions client. */
        @get:Internal
        val service: Property<FunctionServiceClientBuildService>

        /**
         * The full resource name of the function to update, e.g.
         * `projects/my-project/locations/us-central1/functions/my-function`.
         */
        val functionName: Property<String>

        /** The GCS bucket containing the deployment zip. */
        val bucket: Property<String>

        /** The GCS object name of the deployment zip. */
        val storageObject: Property<String>

        /**
         * The GCS object generation to pin. When unset, the latest object version is used.
         */
        val storageGeneration: Property<Long>
    }

    override fun execute() {
        val client = parameters.service.get().getClient()
        val storageSource = StorageSource.newBuilder()
            .setBucket(parameters.bucket.get())
            .setObject(parameters.storageObject.get())
            .also { b -> parameters.storageGeneration.orNull?.let { b.setGeneration(it) } }
            .build()
        val function = client.getFunction(
            GetFunctionRequest.newBuilder().setName(parameters.functionName.get()).build()
        )
        val updated = function.toBuilder()
            .setBuildConfig(
                function.buildConfig.toBuilder()
                    .setSource(Source.newBuilder().setStorageSource(storageSource).build())
                    .build()
            )
            .build()
        client.updateFunctionAsync(
            UpdateFunctionRequest.newBuilder()
                .setFunction(updated)
                .setUpdateMask(FieldMask.newBuilder().addPaths("build_config.source").build())
                .build()
        ).get()
    }
}
