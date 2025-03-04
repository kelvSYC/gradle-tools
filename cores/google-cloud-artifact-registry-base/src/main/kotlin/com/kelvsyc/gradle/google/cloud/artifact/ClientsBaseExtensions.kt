package com.kelvsyc.gradle.google.cloud.artifact

import com.kelvsyc.gradle.clients.ClientsBaseExtension

/**
 * Registers a Google Cloud Service client.
 */
fun ClientsBaseExtension.registerGoogleCloudServiceClient(
    name: String,
    configureAction: ArtifactRegistryClientInfo.() -> Unit
) =
    service.get().registerIfAbsent<ArtifactRegistryClientInfo>(name, configureAction)
