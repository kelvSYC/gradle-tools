package com.kelvsyc.gradle.aws.java

/**
 * Discriminator for which AWS credentials provider to construct from [AwsBuildServiceParams].
 *
 * Set via the extension functions on [AwsBuildServiceParams] rather than directly.
 */
enum class AwsCredentialSource {
    /** Use [software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider]. */
    ANONYMOUS,

    /**
     * Use [software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider], which checks
     * environment variables, `~/.aws/credentials`, EC2/ECS/EKS instance credentials, and other
     * standard sources in order.
     */
    DEFAULT_CHAIN,

    /**
     * Use [software.amazon.awssdk.auth.credentials.StaticCredentialsProvider] built from
     * [AwsBuildServiceParams.accessKeyId], [AwsBuildServiceParams.secretAccessKey], and
     * optionally [AwsBuildServiceParams.sessionToken].
     */
    STATIC,

    /**
     * Use [software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider] for the profile
     * named by [AwsBuildServiceParams.credentialsProfile].
     */
    PROFILE,
}
