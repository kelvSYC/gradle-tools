package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import aws.sdk.kotlin.services.secretsmanager.model.PutSecretValueRequest
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.api.tasks.Internal

/**
 * [WorkAction] implementation that stores a new secret value in an existing Secrets Manager secret.
 *
 * Only string secrets are supported.
 */
abstract class PutSecretValueAction : WorkAction<PutSecretValueAction.Parameters> {
    interface Parameters : WorkParameters {
        /** The shared build service managing Secrets Manager clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of a [SecretsManagerClientInfo]. */
        val clientName: Property<String>

        /** The name or ARN of the secret to update. */
        val secretId: Property<String>

        /** The new secret value (string). */
        val secretString: Property<String>
    }

    private val client: Provider<SecretsManagerClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val request = PutSecretValueRequest {
            secretId = parameters.secretId.get()
            secretString = parameters.secretString.get()
        }

        runBlocking {
            client.get().putSecretValue(request)
        }
    }
}
