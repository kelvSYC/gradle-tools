package com.kelvsyc.gradle.google.cloud.secretmanager

import com.google.api.gax.rpc.ApiException
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest
import com.google.cloud.secretmanager.v1.SecretVersionName
import com.kelvsyc.gradle.logging.GradleLoggerDelegate
import com.kelvsyc.gradle.logging.warn
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation backed by retrieving a secret version from Google Cloud Secret Manager.
 *
 * The secret payload is returned as a UTF-8 string.
 */
abstract class SecretManagerValueSource : ValueSource<String, SecretManagerValueSource.Parameters> {
    companion object {
        val logger by GradleLoggerDelegate
    }

    interface Parameters : ValueSourceParameters {
        /** The build service managing the Secret Manager client. */
        @get:Internal
        val service: Property<SecretManagerServiceClientBuildService>

        val projectId: Property<String>
        val secretId: Property<String>

        /**
         * The version of the secret to retrieve. Defaults to `"latest"` if not set.
         */
        val versionId: Property<String>
    }

    override fun obtain(): String? {
        val version = if (parameters.versionId.isPresent) parameters.versionId.get() else "latest"
        val name = SecretVersionName.of(parameters.projectId.get(), parameters.secretId.get(), version)
        val request = AccessSecretVersionRequest.newBuilder().setName(name.toString()).build()

        return try {
            val response = parameters.service.get().getClient().accessSecretVersion(request)
            response.payload.data.toStringUtf8()
        } catch (e: ApiException) {
            logger.warn(e) { "Unable to retrieve secret '${parameters.secretId.get()}' from Google Cloud Secret Manager" }
            null
        }
    }
}
