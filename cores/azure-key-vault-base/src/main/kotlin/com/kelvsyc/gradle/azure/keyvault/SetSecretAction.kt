package com.kelvsyc.gradle.azure.keyvault

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

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
        /** The build service managing the Key Vault secret client. */
        @get:Internal
        val service: Property<SecretClientBuildService>

        /** The name of the secret to set. */
        val secretName: Property<String>

        /** The secret value to store. */
        val secretValue: Property<String>
    }

    override fun execute() {
        parameters.service.get().getClient()
            .setSecret(parameters.secretName.get(), parameters.secretValue.get())
    }
}
