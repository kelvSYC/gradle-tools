package com.kelvsyc.gradle.aws.java

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.awscore.AwsClient
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder
import software.amazon.awssdk.regions.Region

/**
 * Abstract base class for AWS Java SDK client build services with config-cache-safe parameters.
 *
 * Subclass this and implement [createClient], using [configureBuilder] to apply region and
 * credentials from the shared [AwsBuildServiceParams] to any standard AWS client builder:
 *
 * ```kotlin
 * abstract class SnsClientBuildService : AbstractAwsJavaClientBuildService<SnsClient, AwsBuildServiceParams>() {
 *     override fun createClient(): SnsClient = configureBuilder(SnsClient.builder()).build()
 * }
 * ```
 *
 * Configure the service at registration time using the extension functions on [AwsBuildServiceParams]:
 *
 * ```kotlin
 * gradle.sharedServices.registerIfAbsent("sns", SnsClientBuildService::class) {
 *     parameters {
 *         regionId.set("us-east-1")
 *         defaultCredentials()
 *     }
 * }
 * ```
 *
 * @param C The AWS client type managed by this build service
 * @param P The [AwsBuildServiceParams] subtype; use [AwsBuildServiceParams] directly unless
 *   additional parameters are needed
 */
abstract class AbstractAwsJavaClientBuildService<C : AwsClient, P : AwsBuildServiceParams>
    : AbstractClientBuildService<C, P>() {

    /**
     * Returns the [Region] from [AwsBuildServiceParams.regionId], or `null` if unset.
     *
     * When `null` is returned and passed to a client builder without calling `region()`, the AWS
     * SDK falls back to [DefaultAwsRegionProviderChain][software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain].
     */
    protected fun resolveRegion(): Region? = parameters.regionId.orNull?.let { Region.of(it) }

    /**
     * Constructs an [AwsCredentialsProvider] from [AwsBuildServiceParams.credentialSource] and
     * its supporting fields.
     *
     * | [AwsCredentialSource] | Result |
     * |---|---|
     * | `DEFAULT_CHAIN` | [DefaultCredentialsProvider] |
     * | `STATIC` (no session token) | [StaticCredentialsProvider] with [AwsBasicCredentials] |
     * | `STATIC` (with session token) | [StaticCredentialsProvider] with [AwsSessionCredentials] |
     * | `PROFILE` | [ProfileCredentialsProvider] for the named profile |
     * | `ANONYMOUS` or unset | [AnonymousCredentialsProvider] |
     */
    protected fun resolveCredentialsProvider(): AwsCredentialsProvider =
        when (parameters.credentialSource.orNull) {
            AwsCredentialSource.DEFAULT_CHAIN -> DefaultCredentialsProvider.create()
            AwsCredentialSource.STATIC -> {
                val key = parameters.accessKeyIdRef.get().resolve()
                val secret = parameters.secretAccessKeyRef.get().resolve()
                parameters.sessionTokenRef.orNull
                    ?.let { StaticCredentialsProvider.create(AwsSessionCredentials.create(key, secret, it.resolve())) }
                    ?: StaticCredentialsProvider.create(AwsBasicCredentials.create(key, secret))
            }
            AwsCredentialSource.PROFILE ->
                ProfileCredentialsProvider.create(parameters.credentialsProfile.get())
            AwsCredentialSource.ANONYMOUS, null -> AnonymousCredentialsProvider.create()
        }

    /**
     * Applies [resolveRegion] and [resolveCredentialsProvider] to [builder] and returns it.
     *
     * Works with any standard AWS SDK client builder that extends [AwsClientBuilder].
     */
    protected fun <B> configureBuilder(builder: B): B where B : AwsClientBuilder<B, *> {
        resolveRegion()?.let { builder.region(it) }
        builder.credentialsProvider(resolveCredentialsProvider())
        return builder
    }
}
