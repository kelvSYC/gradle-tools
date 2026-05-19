package com.kelvsyc.gradle.google.cloud.functions

import com.google.cloud.functions.v2.GenerateUploadUrlRequest
import com.google.cloud.functions.v2.GetFunctionRequest
import com.google.cloud.functions.v2.Source
import com.google.cloud.functions.v2.UpdateFunctionRequest
import com.google.protobuf.FieldMask
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that uploads a local zip to Cloud Storage via a signed URL and then
 * updates a Cloud Functions Gen 2 function to use it as the new deployment source.
 *
 * Executes three steps atomically:
 * 1. Calls `generateUploadUrl` to obtain a signed GCS URL and a [com.google.cloud.functions.v2.StorageSource].
 * 2. HTTP PUT the zip bytes to the signed URL.
 * 3. Submits an `updateFunction` long-running operation with the returned source and waits for completion.
 *
 * For a task-level wrapper that declares the zip as a proper Gradle `@InputFile` (enabling
 * up-to-date checking), use [AbstractDeployFunctionFromZip] / [DeployFunctionFromZip].
 */
abstract class UploadAndUpdateFunctionAction :
    WorkAction<UploadAndUpdateFunctionAction.Parameters> {

    /**
     * Parameters for [UploadAndUpdateFunctionAction].
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

        /** The local zip file to upload and deploy. */
        val zipFile: RegularFileProperty
    }

    override fun execute() {
        val client = parameters.service.get().getClient()
        val functionName = parameters.functionName.get()
        val parent = functionName.substringBeforeLast("/functions/")

        val uploadResponse = client.generateUploadUrl(
            GenerateUploadUrlRequest.newBuilder().setParent(parent).build()
        )

        val zipBytes = parameters.zipFile.get().asFile.readBytes()
        OkHttpClient().newCall(
            Request.Builder()
                .url(uploadResponse.uploadUrl)
                .put(zipBytes.toRequestBody("application/zip".toMediaType()))
                .build()
        ).execute().close()

        val function = client.getFunction(
            GetFunctionRequest.newBuilder().setName(functionName).build()
        )
        val updated = function.toBuilder()
            .setBuildConfig(
                function.buildConfig.toBuilder()
                    .setSource(Source.newBuilder().setStorageSource(uploadResponse.storageSource).build())
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
