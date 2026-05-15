package com.kelvsyc.gradle.aws.java

import com.kelvsyc.gradle.clients.CredentialReference
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Config-cache-safe [BuildServiceParameters] for AWS Java SDK client build services.
 *
 * All properties are serializable primitives. Do not set them directly; use the extension functions
 * on this interface (e.g. [anonymous], [defaultCredentials], [staticCredentials], [from]) which
 * atomically set [credentialSource] and its supporting fields together.
 *
 * Credential fields (`accessKeyIdRef`, `secretAccessKeyRef`, `sessionTokenRef`) use [CredentialReference]
 * rather than plain strings to ensure that only lookup metadata — not secret values — is serialized to the
 * Gradle configuration cache.
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
     * [AwsCredentialSource.STATIC]. When absent, [AwsBasicCredentials][software.amazon.awssdk.auth.credentials.AwsBasicCredentials]
     * is used instead of [AwsSessionCredentials][software.amazon.awssdk.auth.credentials.AwsSessionCredentials].
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
