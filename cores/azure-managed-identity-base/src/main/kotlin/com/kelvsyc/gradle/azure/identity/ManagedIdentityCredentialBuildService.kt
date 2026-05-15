package com.kelvsyc.gradle.azure.identity

import com.azure.identity.ManagedIdentityCredential
import com.azure.identity.ManagedIdentityCredentialBuilder
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing a [ManagedIdentityCredential] for a system-assigned or user-assigned managed identity.
 *
 * Register using [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent] and configure the identity
 * selector via the extension functions on [Params]. Leave all params unset for system-assigned identity:
 *
 * ```kotlin
 * val mi = gradle.sharedServices.registerIfAbsent("mi", ManagedIdentityCredentialBuildService::class) {
 *     parameters.systemAssigned()       // explicit no-op; documents intent
 *     // parameters.userAssigned("00000000-0000-0000-0000-000000000000")
 *     // parameters.userAssignedByObjectId("...")
 *     // parameters.userAssignedByResourceId("/subscriptions/.../resourceGroups/.../providers/...")
 * }
 * ```
 */
abstract class ManagedIdentityCredentialBuildService :
    AbstractClientBuildService<ManagedIdentityCredential, ManagedIdentityCredentialBuildService.Params>() {

    /**
     * Configuration parameters for [ManagedIdentityCredentialBuildService].
     *
     * All parameters are optional. When none are set, the build service creates a system-assigned credential.
     * Use only one of [clientId], [objectId], or [resourceId] to select a user-assigned identity.
     */
    interface Params : BuildServiceParameters {
        /** Client/application ID for a user-assigned managed identity. */
        val clientId: Property<String>
        /** Object ID for a user-assigned managed identity. */
        val objectId: Property<String>
        /** ARM resource ID for a user-assigned managed identity. */
        val resourceId: Property<String>
    }

    override fun createClient(): ManagedIdentityCredential {
        val builder = ManagedIdentityCredentialBuilder()
        parameters.clientId.orNull?.let(builder::clientId)
        parameters.objectId.orNull?.let(builder::objectId)
        parameters.resourceId.orNull?.let(builder::resourceId)
        return builder.build()
    }
}
