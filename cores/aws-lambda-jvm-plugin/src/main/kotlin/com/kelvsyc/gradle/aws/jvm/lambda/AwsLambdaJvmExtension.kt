package com.kelvsyc.gradle.aws.jvm.lambda

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

/**
 * Configuration extension for the `aws-lambda-jvm` plugin family.
 *
 * Created by any packaging plugin (fat-package or thin-package) when first applied. Serves as the
 * cross-plugin communication point so that the upload plugin can locate the deployment ZIP without
 * knowing which packaging strategy was used.
 */
abstract class AwsLambdaJvmExtension {
    /** Base name for generated archives. Defaults to the project name. */
    abstract val archiveBaseName: Property<String>

    /**
     * The deployment ZIP file produced by the active packaging plugin.
     *
     * Set automatically by [AwsLambdaJvmFatPackagePlugin] or [AwsLambdaJvmThinPackagePlugin].
     * The upload plugin reads this to wire the ZIP file path to the upload task input.
     */
    abstract val deploymentZipFile: RegularFileProperty
}
