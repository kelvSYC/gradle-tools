package com.kelvsyc.gradle.aws.jvm.lambda.deploy

import com.kelvsyc.gradle.aws.jvm.lambda.AwsLambdaJvmLayeredPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Umbrella plugin that applies [AwsLambdaJvmLayeredPlugin] (thin-JAR packaging) and [AwsLambdaJvmUploadPlugin].
 *
 * Use this instead of [AwsLambdaJvmDeployPlugin] when the Lambda function is backed by a layer
 * that provides its runtime dependencies. The thin-JAR deployment ZIP contains only the function's
 * own compiled classes alongside a `lib/` directory.
 *
 * Configure the target function via the `awsLambdaJvmDeploy` extension and run
 * `./gradlew uploadLambdaFunction` to deploy.
 */
class AwsLambdaJvmLayeredDeployPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply(AwsLambdaJvmLayeredPlugin::class.java)
        project.pluginManager.apply(AwsLambdaJvmUploadPlugin::class.java)
    }
}
