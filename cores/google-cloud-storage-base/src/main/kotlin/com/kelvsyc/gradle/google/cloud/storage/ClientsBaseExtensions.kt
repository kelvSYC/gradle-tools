package com.kelvsyc.gradle.google.cloud.storage

import com.kelvsyc.gradle.clients.ClientsBaseExtension

/**
 * Registers a Google Cloud Service client.
 */
fun ClientsBaseExtension.registerGoogleCloudServiceClient(name: String, configureAction: StorageClientInfo.() -> Unit) =
    service.get().registerIfAbsent<StorageClientInfo>(name, configureAction)
