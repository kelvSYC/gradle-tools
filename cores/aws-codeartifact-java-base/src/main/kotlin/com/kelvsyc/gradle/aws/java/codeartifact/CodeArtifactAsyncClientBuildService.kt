package com.kelvsyc.gradle.aws.java.codeartifact

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.codeartifact.CodeartifactAsyncClient

/**
 * Build service managing an asynchronous [CodeartifactAsyncClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * parameters via the [AwsBuildServiceParams] extension functions as needed. The same registration can then
 * be shared with value sources and work actions via a `Property<CodeArtifactAsyncClientBuildService>` parameter.
 */
abstract class CodeArtifactAsyncClientBuildService : AbstractAwsJavaClientBuildService<CodeartifactAsyncClient, AwsBuildServiceParams>() {
    override fun createClient(): CodeartifactAsyncClient = configureBuilder(CodeartifactAsyncClient.builder()).build()
}
