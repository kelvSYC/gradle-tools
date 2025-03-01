package com.kelvsyc.gradle.aws.java.codeartifact

import com.kelvsyc.gradle.clients.ClientsBaseExtension

/**
 * Registers a synchronous AWS CodeArtifact Java Client.
 */
fun ClientsBaseExtension.registerAwsCodeArtifactJavaClient(
    name: String,
    configureAction: CodeArtifactClientInfo.() -> Unit
) = service.get().registerIfAbsent(name, configureAction)

/**
 * Registers an asynchronous AWS CodeArtifact Java Client.
 */
fun ClientsBaseExtension.registerAwsCodeArtifactAsyncJavaClient(
    name: String,
    configureAction: CodeArtifactAsyncClientInfo.() -> Unit
) = service.get().registerIfAbsent(name, configureAction)
