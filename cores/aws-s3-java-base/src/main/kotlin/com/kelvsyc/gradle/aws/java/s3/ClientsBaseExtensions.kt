package com.kelvsyc.gradle.aws.java.s3

import com.kelvsyc.gradle.clients.ClientsBaseExtension

/**
 * Registers a synchronous AWS S3 Java Client.
 */
fun ClientsBaseExtension.registerAwsS3JavaClient(name: String, configureAction: S3ClientInfo.() -> Unit) =
    service.get().registerIfAbsent(name, configureAction)

/**
 * Registers an asynchronous AWS S3 Java Client.
 */
fun ClientsBaseExtension.registerAwsS3AsyncJavaClient(name: String, configureAction: S3AsyncClientInfo.() -> Unit) =
    service.get().registerIfAbsent(name, configureAction)

/**
 * Registers an AWS S3 Transfer Manager Client.
 */
fun ClientsBaseExtension.registerAwsS3TransferManagerJavaClient(
    name: String,
    configureAction: S3TransferManagerClientInfo.() -> Unit
) =
    service.get().registerIfAbsent(name, configureAction)
