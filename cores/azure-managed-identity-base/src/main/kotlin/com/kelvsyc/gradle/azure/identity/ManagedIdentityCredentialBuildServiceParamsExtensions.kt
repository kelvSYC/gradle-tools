package com.kelvsyc.gradle.azure.identity

import org.gradle.api.provider.Provider

/**
 * Selects the system-assigned managed identity. This is a no-op (all params remain unset) and exists
 * solely to document intent at the call site.
 */
fun ManagedIdentityCredentialBuildService.Params.systemAssigned() {
    // intentionally empty — system-assigned is the default when no params are set
}

/** Selects a user-assigned managed identity by client/application ID. */
fun ManagedIdentityCredentialBuildService.Params.userAssigned(clientId: String) {
    this.clientId.set(clientId)
}

/** Selects a user-assigned managed identity by client/application ID from a [Provider]. */
fun ManagedIdentityCredentialBuildService.Params.userAssigned(clientId: Provider<String>) {
    this.clientId.set(clientId)
}

/** Selects a user-assigned managed identity by object ID. */
fun ManagedIdentityCredentialBuildService.Params.userAssignedByObjectId(objectId: String) {
    this.objectId.set(objectId)
}

/** Selects a user-assigned managed identity by object ID from a [Provider]. */
fun ManagedIdentityCredentialBuildService.Params.userAssignedByObjectId(objectId: Provider<String>) {
    this.objectId.set(objectId)
}

/** Selects a user-assigned managed identity by ARM resource ID. */
fun ManagedIdentityCredentialBuildService.Params.userAssignedByResourceId(resourceId: String) {
    this.resourceId.set(resourceId)
}

/** Selects a user-assigned managed identity by ARM resource ID from a [Provider]. */
fun ManagedIdentityCredentialBuildService.Params.userAssignedByResourceId(resourceId: Provider<String>) {
    this.resourceId.set(resourceId)
}
