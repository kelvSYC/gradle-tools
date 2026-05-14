package com.kelvsyc.gradle.aws.kotlin

/**
 * Discriminator for which AWS credentials provider to construct from [AwsBuildServiceParams].
 *
 * Set via the extension functions on [AwsBuildServiceParams] rather than directly.
 */
enum class AwsCredentialSource {
    /**
     * No credentials provider. The AWS SDK for Kotlin client receives no `credentialsProvider`
     * assignment and falls back to its default behavior.
     */
    ANONYMOUS,

    /**
     * Use [aws.sdk.kotlin.runtime.auth.credentials.DefaultChainCredentialsProvider], which checks
     * environment variables, `~/.aws/credentials`, EC2/ECS/EKS instance credentials, and other
     * standard sources in order.
     */
    DEFAULT_CHAIN,

    /**
     * Use [aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider] built from
     * [AwsBuildServiceParams.accessKeyId], [AwsBuildServiceParams.secretAccessKey], and
     * optionally [AwsBuildServiceParams.sessionToken].
     */
    STATIC,

    /**
     * Use [aws.sdk.kotlin.runtime.auth.credentials.ProfileCredentialsProvider] for the profile
     * named by [AwsBuildServiceParams.credentialsProfile].
     */
    PROFILE,
}
