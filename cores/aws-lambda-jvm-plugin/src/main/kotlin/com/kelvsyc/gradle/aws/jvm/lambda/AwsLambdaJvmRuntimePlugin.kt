package com.kelvsyc.gradle.aws.jvm.lambda

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Adds the `aws-lambda-java-core` runtime dependency to the applied project.
 *
 * This is an atomic plugin providing only the Lambda runtime interfaces. Apply it when you want the
 * `com.amazonaws:aws-lambda-java-core` handler API without packaging tasks. It is applied
 * automatically by [AwsLambdaJvmPlugin] and [AwsLambdaJvmLayeredPlugin].
 */
class AwsLambdaJvmRuntimePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.dependencies.add("implementation", "com.amazonaws:aws-lambda-java-core:$LAMBDA_CORE_VERSION")
    }

    companion object {
        /** Version of `aws-lambda-java-core` added to the project's `implementation` configuration. */
        const val LAMBDA_CORE_VERSION = "1.2.3"
    }
}
