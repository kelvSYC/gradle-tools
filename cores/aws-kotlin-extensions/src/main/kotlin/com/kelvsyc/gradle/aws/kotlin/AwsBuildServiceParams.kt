package com.kelvsyc.gradle.aws.kotlin

import com.kelvsyc.gradle.clients.CredentialReference
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Config-cache-safe [BuildServiceParameters] for AWS Kotlin SDK client build services.
 *
 * All properties are serializable primitives. Do not set [credentialSource] and its supporting
 * fields directly; use the extension functions on this interface (e.g. [anonymous],
 * [defaultCredentials], [staticCredentials], [from]) which atomically configure them together.
 *
 * Credential fields (`accessKeyIdRef`, `secretAccessKeyRef`, `sessionTokenRef`) use [CredentialReference]
 * rather than plain strings to ensure that only lookup metadata — not secret values — is serialized to the
 * Gradle configuration cache.
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
     * AWS access key ID reference. Used when [credentialSource] is [AwsCredentialSource.STATIC].
     *
     * Stores a [CredentialReference] pointing to where the access key ID can be found at execution time.
     * Set via [staticCredentials], [sessionCredentials], or [from].
     */
    val accessKeyIdRef: Property<CredentialReference>

    /**
     * AWS secret access key reference. Used when [credentialSource] is [AwsCredentialSource.STATIC].
     *
     * Stores a [CredentialReference] pointing to where the secret access key can be found at execution time.
     * Set via [staticCredentials], [sessionCredentials], or [from].
     */
    val secretAccessKeyRef: Property<CredentialReference>

    /**
     * AWS session token reference for temporary credentials. Optional; used when [credentialSource] is
     * [AwsCredentialSource.STATIC]. When absent, the underlying
     * [aws.smithy.kotlin.runtime.auth.awscredentials.Credentials] is constructed without a
     * session token.
     *
     * Stores a [CredentialReference] pointing to where the session token can be found at execution time.
     * Set via [sessionCredentials] or [from].
     */
    val sessionTokenRef: Property<CredentialReference>

    /**
     * Named AWS credentials profile. Used when [credentialSource] is [AwsCredentialSource.PROFILE].
     *
     * Set via [profileCredentials].
     */
    val credentialsProfile: Property<String>
}
