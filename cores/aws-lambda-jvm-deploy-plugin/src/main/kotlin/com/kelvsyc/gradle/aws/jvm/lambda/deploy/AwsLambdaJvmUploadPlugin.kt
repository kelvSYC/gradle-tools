package com.kelvsyc.gradle.aws.jvm.lambda.deploy

import com.kelvsyc.gradle.aws.java.defaultCredentials
import com.kelvsyc.gradle.aws.java.lambda.LambdaClientBuildService
import com.kelvsyc.gradle.aws.java.lambda.UpdateFunctionCodeTask
import com.kelvsyc.gradle.aws.jvm.lambda.AwsLambdaJvmExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

/**
 * Atomic plugin that registers an `uploadLambdaFunction` task wired to the deployment ZIP produced
 * by the `aws-lambda-jvm.fat-package` or `aws-lambda-jvm.thin-package` plugin.
 *
 * The plugin auto-registers a [LambdaClientBuildService] using the default AWS credential chain
 * and wires it directly to the task. Configure the target function and optional region via the
 * `awsLambdaJvmDeploy` extension:
 *
 * ```kotlin
 * awsLambdaJvmDeploy {
 *     functionName.set("my-function")
 *     publish.set(true)
 *     regionId.set("us-east-1")  // optional; defaults to SDK provider chain
 * }
 * ```
 *
 * This plugin is applied automatically by [AwsLambdaJvmDeployPlugin] and
 * [AwsLambdaJvmLayeredDeployPlugin]. Apply it directly when you want upload wiring without the
 * bundled packaging plugins.
 *
 * **Plugin application order is irrelevant.** The upload task's `zipFile` input is wired to
 * [AwsLambdaJvmExtension.deploymentZipFile] via a lazy provider chain resolved at task execution
 * time, after all plugins have been applied.
 */
class AwsLambdaJvmUploadPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val packagingExtension = project.extensions.findByType(AwsLambdaJvmExtension::class.java)
            ?: project.extensions.create("awsLambdaJvm", AwsLambdaJvmExtension::class.java).also {
                it.archiveBaseName.convention(project.name)
            }

        val deployExtension = project.extensions.create(
            "awsLambdaJvmDeploy",
            AwsLambdaJvmDeployExtension::class.java,
        )

        val lambdaService = project.gradle.sharedServices.registerIfAbsent(
            "awsLambdaJvmLambdaClient",
            LambdaClientBuildService::class.java,
        ) {
            parameters {
                regionId.set(deployExtension.regionId)
                defaultCredentials()
            }
        }

        project.tasks.register<UpdateFunctionCodeTask>("uploadLambdaFunction") {
            service.set(lambdaService)
            functionName.set(deployExtension.functionName)
            zipFile.set(packagingExtension.deploymentZipFile)
            publish.set(deployExtension.publish)
        }
    }
}
