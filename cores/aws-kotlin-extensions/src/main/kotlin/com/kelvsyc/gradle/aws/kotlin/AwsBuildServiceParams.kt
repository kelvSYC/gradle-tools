package com.kelvsyc.gradle.aws.kotlin

import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Config-cache-safe [BuildServiceParameters] for AWS Kotlin SDK client build services.
 *
 * All properties are serializable primitives. Do not set [credentialSource] and its supporting
 * fields directly; use the extension functions on this interface (e.g. [anonymous],
 * [defaultCredentials], [staticCredentials], [from]) which atomically configure them together.
 */
interface AwsBuildServiceParams : BuildServiceParameters {
    /**
     * AWS region identifier, e.g. `"us-east-1"`.
     *
     * Leave unset to delegate region resolution to the AWS SDK for Kotlin's default region
     * provider chain.
     */
    val region: Property<String>

    /**
     * Which credentials provider to construct. Leave unset for anonymous mode (no
     * `credentialsProvider` is assigned to the client; the SDK falls back to its default).
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
     * [AwsCredentialSource.STATIC]. When absent, the underlying
     * [aws.smithy.kotlin.runtime.auth.awscredentials.Credentials] is constructed without a
     * session token.
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
