package com.kelvsyc.gradle.aws.java.codeartifact

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import software.amazon.awssdk.services.codeartifact.CodeartifactClient

/**
 * Build service managing a synchronous [CodeartifactClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * parameters via the [AwsBuildServiceParams] extension functions as needed. The same registration can then
 * be shared with value sources and work actions via a `Property<CodeArtifactClientBuildService>` parameter.
 */
abstract class CodeArtifactClientBuildService : AbstractAwsJavaClientBuildService<CodeartifactClient, AwsBuildServiceParams>() {
    override fun createClient(): CodeartifactClient = configureBuilder(CodeartifactClient.builder()).build()
}
