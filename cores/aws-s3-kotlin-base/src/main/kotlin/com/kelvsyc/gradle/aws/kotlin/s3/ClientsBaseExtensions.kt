package com.kelvsyc.gradle.aws.kotlin.s3

import com.kelvsyc.gradle.clients.ClientsBaseExtension

/**
 * Registers a synchronous AWS S3 Kotlin Client.
 */
fun ClientsBaseExtension.registerAwsS3KotlinClient(name: String, configureAction: S3ClientInfo.() -> Unit) =
    service.get().registerIfAbsent(name, configureAction)
