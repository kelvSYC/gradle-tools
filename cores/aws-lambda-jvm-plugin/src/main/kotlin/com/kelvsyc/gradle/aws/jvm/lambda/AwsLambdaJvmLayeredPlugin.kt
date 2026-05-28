package com.kelvsyc.gradle.aws.jvm.lambda

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Umbrella plugin that applies [AwsLambdaJvmRuntimePlugin] and [AwsLambdaJvmThinPackagePlugin].
 *
 * Use this instead of [AwsLambdaJvmPlugin] when the Lambda function is backed by a layer that
 * provides its runtime dependencies. The thin-JAR deployment ZIP is small — it contains only the
 * function's own compiled classes alongside a `lib/` directory of runtime JARs — so deployments
 * are fast when the dependency set is large but changes infrequently.
 *
 * To upload the deployment ZIP to AWS Lambda, also apply `aws-lambda-jvm.layered-deploy` from
 * the `aws-lambda-jvm-deploy-plugin` component.
 */
class AwsLambdaJvmLayeredPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply(AwsLambdaJvmRuntimePlugin::class.java)
        project.pluginManager.apply(AwsLambdaJvmThinPackagePlugin::class.java)
    }
}
