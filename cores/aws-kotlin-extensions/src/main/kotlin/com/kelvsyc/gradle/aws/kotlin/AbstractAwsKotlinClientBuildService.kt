package com.kelvsyc.gradle.aws.kotlin

import aws.sdk.kotlin.runtime.auth.credentials.DefaultChainCredentialsProvider
import aws.sdk.kotlin.runtime.auth.credentials.ProfileCredentialsProvider
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.client.SdkClient
import com.kelvsyc.gradle.clients.AbstractClientBuildService

/**
 * Abstract base class for AWS Kotlin SDK client build services with config-cache-safe parameters.
 *
 * Because the AWS Kotlin SDK exposes a service-specific DSL builder per client (rather than a
 * shared fluent builder type), subclasses apply region and credentials by reading
 * [resolveRegion] and [resolveCredentialsProvider] from inside their DSL block:
 *
 * ```kotlin
 * abstract class SqsClientBuildService :
 *     AbstractAwsKotlinClientBuildService<SqsClient, AwsBuildServiceParams>() {
 *     override fun createClient(): SqsClient = SqsClient {
 *         resolveRegion()?.let { region = it }
 *         resolveCredentialsProvider()?.let { credentialsProvider = it }
 *     }
 * }
 * ```
 *
 * Configure the service at registration time using the extension functions on
 * [AwsBuildServiceParams]:
 *
 * ```kotlin
 * gradle.sharedServices.registerIfAbsent("sqs", SqsClientBuildService::class) {
 *     parameters {
 *         region.set("us-east-1")
 *         defaultCredentials()
 *     }
 * }
 * ```
 *
 * @param C The AWS Kotlin SDK client type managed by this build service
 * @param P The [AwsBuildServiceParams] subtype; use [AwsBuildServiceParams] directly unless
 *   additional parameters are needed
 */
abstract class AbstractAwsKotlinClientBuildService<C : SdkClient, P : AwsBuildServiceParams>
    : AbstractClientBuildService<C, P>() {

    /**
     * Returns the region identifier from [AwsBuildServiceParams.region], or `null` if unset.
     *
     * When `null` is returned and not assigned to the client DSL builder's `region` property, the
     * AWS SDK for Kotlin falls back to its default region provider chain.
     */
    protected fun resolveRegion(): String? = parameters.region.orNull

    /**
     * Constructs a [CredentialsProvider] from [AwsBuildServiceParams.credentialSource] and its
     * supporting fields.
     *
     * | [AwsCredentialSource] | Result |
     * |---|---|
     * | `DEFAULT_CHAIN` | [DefaultChainCredentialsProvider] |
     * | `STATIC` (no session token) | [StaticCredentialsProvider] with basic [Credentials] |
     * | `STATIC` (with session token) | [StaticCredentialsProvider] with session [Credentials] |
     * | `PROFILE` | [ProfileCredentialsProvider] for the named profile |
     * | `ANONYMOUS` or unset | `null` — caller skips the `credentialsProvider` assignment |
     */
    protected fun resolveCredentialsProvider(): CredentialsProvider? =
        when (parameters.credentialSource.orNull) {
            AwsCredentialSource.DEFAULT_CHAIN -> DefaultChainCredentialsProvider()
            AwsCredentialSource.STATIC -> {
                val key = parameters.accessKeyId.get()
                val secret = parameters.secretAccessKey.get()
                val credentials = parameters.sessionToken.orNull
                    ?.let { Credentials(key, secret, it) }
                    ?: Credentials(key, secret)
                StaticCredentialsProvider(credentials)
            }
            AwsCredentialSource.PROFILE ->
                ProfileCredentialsProvider(profileName = parameters.credentialsProfile.get())
            AwsCredentialSource.ANONYMOUS, null -> null
        }
}
