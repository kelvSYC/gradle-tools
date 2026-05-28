package com.kelvsyc.gradle.aws.jvm.lambda.deploy

import org.gradle.api.provider.Property

/**
 * Configuration extension for the `aws-lambda-jvm` upload plugin.
 *
 * Configure this extension to specify the target Lambda function and upload options.
 * The `awsLambdaJvmDeploy` extension is created automatically when any of the upload
 * plugins is applied.
 *
 * ```kotlin
 * awsLambdaJvmDeploy {
 *     functionName.set("my-lambda-function")
 *     publish.set(true)
 *     regionId.set("us-east-1")
 * }
 * ```
 */
abstract class AwsLambdaJvmDeployExtension {
    /** Lambda function name, partial ARN, or full ARN. Required before running `uploadLambdaFunction`. */
    abstract val functionName: Property<String>

    /** Whether to publish a new function version after upload. Defaults to `false` when absent. */
    abstract val publish: Property<Boolean>

    /**
     * AWS region ID for the Lambda client (e.g. `"us-east-1"`). Optional — when absent, the SDK
     * resolves the region from the standard provider chain (`AWS_REGION` env var, `~/.aws/config`,
     * EC2 instance metadata, etc.).
     */
    abstract val regionId: Property<String>
}
