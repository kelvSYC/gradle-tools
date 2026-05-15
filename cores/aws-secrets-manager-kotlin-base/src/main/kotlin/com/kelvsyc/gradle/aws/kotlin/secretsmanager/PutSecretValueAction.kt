package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.model.PutSecretValueRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that stores a new secret value in an existing Secrets Manager secret.
 *
 * Only string secrets are supported.
 */
abstract class PutSecretValueAction : WorkAction<PutSecretValueAction.Parameters> {
    /**
     * Parameters for [PutSecretValueAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the Secrets Manager client. */
        @get:Internal
        val service: Property<SecretsManagerClientBuildService>

        /** The name or ARN of the secret to update. */
        val secretId: Property<String>

        /** The new secret value (string). */
        val secretString: Property<String>
    }

    override fun execute() {
        val request = PutSecretValueRequest {
            secretId = parameters.secretId.get()
            secretString = parameters.secretString.get()
        }

        runBlocking {
            parameters.service.get().getClient().putSecretValue(request)
        }
    }
}
