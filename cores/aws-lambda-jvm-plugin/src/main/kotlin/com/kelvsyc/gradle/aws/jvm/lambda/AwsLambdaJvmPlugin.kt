package com.kelvsyc.gradle.aws.jvm.lambda

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Umbrella plugin that applies [AwsLambdaJvmRuntimePlugin] and [AwsLambdaJvmFatPackagePlugin].
 *
 * This is the recommended entry point for most JVM Lambda projects. It adds the
 * `aws-lambda-java-core` handler API to `implementation` and registers `lambdaFatJar` /
 * `lambdaDeploymentZip` tasks that produce a single fat JAR deployment package.
 *
 * To upload the deployment ZIP to AWS Lambda, also apply `aws-lambda-jvm.deploy` from the
 * `aws-lambda-jvm-deploy-plugin` component.
 */
class AwsLambdaJvmPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply(AwsLambdaJvmRuntimePlugin::class.java)
        project.pluginManager.apply(AwsLambdaJvmFatPackagePlugin::class.java)
    }
}
