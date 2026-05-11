package com.kelvsyc.gradle.azure.storage.blob

import com.kelvsyc.gradle.clients.ClientsBaseExtension

/**
 * Registers a synchronous Azure [BlobServiceClientInfo].
 */
fun ClientsBaseExtension.registerAzureBlobServiceClient(
    name: String,
    configureAction: BlobServiceClientInfo.() -> Unit
) = service.get().registerIfAbsent(name, configureAction)

/**
 * Registers an asynchronous Azure [BlobServiceAsyncClientInfo].
 */
fun ClientsBaseExtension.registerAzureBlobServiceAsyncClient(
    name: String,
    configureAction: BlobServiceAsyncClientInfo.() -> Unit
) = service.get().registerIfAbsent(name, configureAction)

/**
 * Registers a synchronous Azure [BlobContainerClientInfo].
 */
fun ClientsBaseExtension.registerAzureBlobContainerClient(
    name: String,
    configureAction: BlobContainerClientInfo.() -> Unit
) = service.get().registerIfAbsent(name, configureAction)

/**
 * Registers an asynchronous Azure [BlobContainerAsyncClientInfo].
 */
fun ClientsBaseExtension.registerAzureBlobContainerAsyncClient(
    name: String,
    configureAction: BlobContainerAsyncClientInfo.() -> Unit
) = service.get().registerIfAbsent(name, configureAction)
