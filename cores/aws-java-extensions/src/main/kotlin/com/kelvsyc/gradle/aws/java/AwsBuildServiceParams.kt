package com.kelvsyc.gradle.aws.java

import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Config-cache-safe [BuildServiceParameters] for AWS Java SDK client build services.
 *
 * All properties are serializable primitives. Do not set them directly; use the extension functions
 * on this interface (e.g. [anonymous], [defaultCredentials], [staticCredentials], [from]) which
 * atomically set [credentialSource] and its supporting fields together.
 */
interface AwsBuildServiceParams : BuildServiceParameters {
    /**
     * AWS region identifier, e.g. `"us-east-1"`.
     *
     * Leave unset to delegate region resolution to
     * [DefaultAwsRegionProviderChain][software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain].
     */
    val regionId: Property<String>

    /**
     * Which credentials provider to construct. Leave unset to use
     * [AnonymousCredentialsProvider][software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider].
     *
     * Use the extension functions rather than setting this directly.
     */
    val credentialSource: Property<AwsCredentialSource>

    /**
     * AWS access key ID. Used when [credentialSource] is [AwsCredentialSource.STATIC].
     *
     * Set via [staticCredentials], [sessionCredentials], or [from].
     */
    val accessKeyId: Property<String>

    /**
     * AWS secret access key. Used when [credentialSource] is [AwsCredentialSource.STATIC].
     *
     * Set via [staticCredentials], [sessionCredentials], or [from].
     */
    val secretAccessKey: Property<String>

    /**
     * AWS session token for temporary credentials. Optional; used when [credentialSource] is
     * [AwsCredentialSource.STATIC]. When absent, [AwsBasicCredentials][software.amazon.awssdk.auth.credentials.AwsBasicCredentials]
     * is used instead of [AwsSessionCredentials][software.amazon.awssdk.auth.credentials.AwsSessionCredentials].
     *
     * Set via [sessionCredentials] or [from].
     */
    val sessionToken: Property<String>

    /**
     * Named AWS credentials profile. Used when [credentialSource] is [AwsCredentialSource.PROFILE].
     *
     * Set via [profileCredentials].
     */
    val credentialsProfile: Property<String>
}
