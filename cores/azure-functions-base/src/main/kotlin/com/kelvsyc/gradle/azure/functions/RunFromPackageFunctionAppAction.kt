package com.kelvsyc.gradle.azure.functions

import com.kelvsyc.gradle.clients.CredentialReference
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that sets the `WEBSITE_RUN_FROM_PACKAGE` app setting on an Azure Function App,
 * causing Azure to mount the specified blob URL as the function package on next cold start.
 *
 * Use [plainUrl] for plain blob URLs (Managed Identity or anonymous access). Use [sasUrl] for
 * SAS-authenticated blob URLs — the SAS URL is resolved from a [CredentialReference] at execution
 * time and never serialized to the configuration cache.
 *
 * [Parameters.packageUrl] and [Parameters.packageSasUrlRef] are mutually exclusive. Use the
 * convenience extensions rather than setting them directly.
 */
abstract class RunFromPackageFunctionAppAction : WorkAction<RunFromPackageFunctionAppAction.Parameters> {

    /**
     * Parameters for [RunFromPackageFunctionAppAction].
     */
    interface Parameters : WorkParameters {
        /** The ARM manager service. Resource group is read from its own params. */
        @get:Internal
        val appService: Property<FunctionAppClientBuildService>

        /** The name of the function app to update. */
        val appName: Property<String>

        /**
         * Plain blob URL for Managed Identity or anonymous blob access. Set via [plainUrl].
         * Mutually exclusive with [packageSasUrlRef].
         */
        val packageUrl: Property<String>

        /**
         * Reference to the full SAS URL (including token) stored in an environment variable or
         * system property. Set via [sasUrl]. Mutually exclusive with [packageUrl].
         */
        @get:Internal
        val packageSasUrlRef: Property<CredentialReference>
    }

    override fun execute() {
        val resolvedUrl = when {
            parameters.packageSasUrlRef.isPresent -> parameters.packageSasUrlRef.get().resolve()
            parameters.packageUrl.isPresent -> parameters.packageUrl.get()
            else -> error("Either packageUrl or packageSasUrlRef must be set on RunFromPackageFunctionAppAction")
        }
        val manager = parameters.appService.get().getClient()
        val resourceGroup = parameters.appService.get().parameters.resourceGroup.get()
        manager.functionApps()
            .getByResourceGroup(resourceGroup, parameters.appName.get())
            .update()
            .withAppSetting("WEBSITE_RUN_FROM_PACKAGE", resolvedUrl)
            .apply()
    }
}

/** Sets [RunFromPackageFunctionAppAction.Parameters.packageUrl] to the provided plain URL. */
fun RunFromPackageFunctionAppAction.Parameters.plainUrl(url: String) {
    packageUrl.set(url)
}

/**
 * Sets [RunFromPackageFunctionAppAction.Parameters.packageSasUrlRef] to the provided reference.
 * The full SAS URL (including token) is resolved from the reference at execution time.
 * Use [CredentialReference.EnvironmentVariable] or [CredentialReference.SystemProperty] — not
 * [CredentialReference.Literal] for production use.
 */
fun RunFromPackageFunctionAppAction.Parameters.sasUrl(ref: CredentialReference) {
    packageSasUrlRef.set(ref)
}
