package com.kelvsyc.gradle.aws.java.secretsmanager

import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest

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
        val service: Property<SecretsManagerClientBuildService>

        /** The name or ARN of the secret to update. */
        val secretId: Property<String>

        /** The new secret value (string). */
        val secretString: Property<String>
    }

    override fun execute() {
        val request = PutSecretValueRequest.builder()
            .secretId(parameters.secretId.get())
            .secretString(parameters.secretString.get())
            .build()

        parameters.service.get().getClient().putSecretValue(request)
    }
}
