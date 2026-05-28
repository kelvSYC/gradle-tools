package com.kelvsyc.gradle.aws.jvm.lambda.deploy

import com.kelvsyc.gradle.aws.jvm.lambda.AwsLambdaJvmPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Umbrella plugin that applies [AwsLambdaJvmPlugin] (fat-JAR packaging) and [AwsLambdaJvmUploadPlugin].
 *
 * This is the recommended entry point for JVM Lambda projects that build a fat JAR and upload it
 * directly to AWS Lambda. Configure the target function via the `awsLambdaJvmDeploy` extension and
 * run `./gradlew uploadLambdaFunction` to deploy.
 */
class AwsLambdaJvmDeployPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply(AwsLambdaJvmPlugin::class.java)
        project.pluginManager.apply(AwsLambdaJvmUploadPlugin::class.java)
    }
}
