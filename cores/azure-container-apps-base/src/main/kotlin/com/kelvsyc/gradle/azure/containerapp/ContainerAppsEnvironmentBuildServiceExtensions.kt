package com.kelvsyc.gradle.azure.containerapp

import com.kelvsyc.gradle.azure.AzureCredentialSource
import com.kelvsyc.gradle.clients.CredentialReference

/**
 * Configures the service to use [com.azure.identity.DefaultAzureCredential] — the standard
 * chain: environment variables -> workload identity -> managed identity -> Azure CLI -> IDE.
 */
fun ContainerAppsEnvironmentBuildService.Params.defaultCredential() {
    credentialSource.set(AzureCredentialSource.DEFAULT)
}

/**
 * Configures the service to use a user-assigned or system-assigned Managed Identity.
 *
 * @param clientId Optional client ID for a user-assigned identity. Omit for system-assigned.
 */
fun ContainerAppsEnvironmentBuildService.Params.managedIdentity(clientId: String? = null) {
    credentialSource.set(AzureCredentialSource.MANAGED_IDENTITY)
    clientId?.let { this.clientId.set(it) }
}

/**
 * Configures the service to use a service principal (client secret).
 *
 * @param tenantId Azure AD tenant ID.
 * @param clientId Azure AD application (client) ID.
 * @param secret [CredentialReference] pointing to the client secret value.
 */
fun ContainerAppsEnvironmentBuildService.Params.clientSecret(
    tenantId: String,
    clientId: String,
    secret: CredentialReference,
) {
    credentialSource.set(AzureCredentialSource.CLIENT_SECRET)
    this.tenantId.set(tenantId)
    this.clientId.set(clientId)
    clientSecretRef.set(secret)
}
