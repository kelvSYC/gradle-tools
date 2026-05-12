package com.kelvsyc.gradle.azure.keyvault

import com.azure.security.keyvault.secrets.SecretClient
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.api.tasks.Internal

/**
 * [WorkAction] implementation that stores a secret in Azure Key Vault.
 *
 * If the secret already exists, a new version is created.
 */
abstract class SetSecretAction : WorkAction<SetSecretAction.Parameters> {
    /**
     * Parameters for [SetSecretAction].
     */
    interface Parameters : WorkParameters {
        /** The shared build service managing Key Vault clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of a [SecretClientInfo]. */
        val clientName: Property<String>

        /** The name of the secret to set. */
        val secretName: Property<String>

        /** The secret value to store. */
        val secretValue: Property<String>
    }

    private val client: Provider<SecretClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        client.get().setSecret(parameters.secretName.get(), parameters.secretValue.get())
    }
}
