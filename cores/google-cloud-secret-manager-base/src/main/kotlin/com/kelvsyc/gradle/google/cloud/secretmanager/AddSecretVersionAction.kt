package com.kelvsyc.gradle.google.cloud.secretmanager

import com.google.cloud.secretmanager.v1.AddSecretVersionRequest
import com.google.cloud.secretmanager.v1.SecretName
import com.google.cloud.secretmanager.v1.SecretPayload
import com.google.protobuf.ByteString
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that adds a new version to an existing Google Cloud Secret Manager secret.
 *
 * Only string (UTF-8) payloads are supported.
 */
abstract class AddSecretVersionAction : WorkAction<AddSecretVersionAction.Parameters> {
    /**
     * Parameters for [AddSecretVersionAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the Secret Manager client. */
        @get:Internal
        val service: Property<SecretManagerServiceClientBuildService>

        /** GCP project ID. */
        val projectId: Property<String>

        /** The secret ID to add a version to. */
        val secretId: Property<String>

        /** The new secret payload as a UTF-8 string. */
        val payload: Property<String>
    }

    override fun execute() {
        val parent = SecretName.of(parameters.projectId.get(), parameters.secretId.get())
        val secretPayload = SecretPayload.newBuilder()
            .setData(ByteString.copyFromUtf8(parameters.payload.get()))
            .build()
        val request = AddSecretVersionRequest.newBuilder()
            .setParent(parent.toString())
            .setPayload(secretPayload)
            .build()

        parameters.service.get().getClient().addSecretVersion(request)
    }
}
