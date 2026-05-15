package com.kelvsyc.gradle.google.cloud

import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.api.gax.core.NoCredentialsProvider
import com.google.auth.Credentials
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.NoCredentials
import com.kelvsyc.gradle.clients.AbstractClientBuildService

/**
 * Abstract base class for Google Cloud client build services with config-cache-safe parameters.
 *
 * Subclass this and implement [createClient], using [resolveCredentials] (for clients that take a
 * [Credentials] directly, e.g. `StorageOptions.Builder.setCredentials`) or
 * [resolveCredentialsProvider] (for clients that take a `gax` [CredentialsProvider], e.g.
 * `TopicAdminSettings`):
 *
 * ```kotlin
 * abstract class StorageClientBuildService : AbstractGcpClientBuildService<Storage, GcpBuildServiceParams>() {
 *     override fun createClient(): Storage = StorageOptions.newBuilder().apply {
 *         parameters.projectId.orNull?.let { setProjectId(it) }
 *         resolveCredentials()?.let { setCredentials(it) }
 *     }.build().service
 * }
 * ```
 *
 * Configure the service at registration time using the extension functions on
 * [GcpBuildServiceParams]:
 *
 * ```kotlin
 * gradle.sharedServices.registerIfAbsent("storage", StorageClientBuildService::class) {
 *     parameters {
 *         projectId.set("my-project")
 *         applicationDefault()
 *     }
 * }
 * ```
 *
 * @param C The Google Cloud client type managed by this build service
 * @param P The [GcpBuildServiceParams] subtype; use [GcpBuildServiceParams] directly unless
 *   additional parameters are needed
 */
abstract class AbstractGcpClientBuildService<C : Any, P : GcpBuildServiceParams>
    : AbstractClientBuildService<C, P>() {

    /**
     * Constructs a [Credentials] from [GcpBuildServiceParams.credentialSource] and its supporting
     * fields, or returns `null` when [GcpBuildServiceParams.credentialSource] is unset (in which
     * case the calling client builder should not have `setCredentials` called on it, so the SDK
     * applies its own default resolution).
     *
     * | [GcpCredentialSource] | Result |
     * |---|---|
     * | `NONE` | [NoCredentials.getInstance] |
     * | `APPLICATION_DEFAULT` | [GoogleCredentials.getApplicationDefault] |
     * | `SERVICE_ACCOUNT_JSON_FILE` | [ServiceAccountCredentials.fromStream] over the credentials file |
     * | `SERVICE_ACCOUNT_JSON_ENV` | [ServiceAccountCredentials.fromStream] over JSON resolved from [GcpBuildServiceParams.credentialsJsonRef] |
     * | `ACCESS_TOKEN` | [GoogleCredentials.create] over an [AccessToken] resolved from [GcpBuildServiceParams.accessTokenRef] |
     * | unset | `null` |
     */
    protected fun resolveCredentials(): Credentials? = when (parameters.credentialSource.orNull) {
        GcpCredentialSource.NONE -> NoCredentials.getInstance()
        GcpCredentialSource.APPLICATION_DEFAULT -> GoogleCredentials.getApplicationDefault()
        GcpCredentialSource.SERVICE_ACCOUNT_JSON_FILE ->
            parameters.credentialsFile.get().asFile.inputStream().use(ServiceAccountCredentials::fromStream)
        GcpCredentialSource.SERVICE_ACCOUNT_JSON_ENV ->
            parameters.credentialsJsonRef.get().resolve().byteInputStream().use(ServiceAccountCredentials::fromStream)
        GcpCredentialSource.ACCESS_TOKEN ->
            GoogleCredentials.create(AccessToken(parameters.accessTokenRef.get().resolve(), null))
        null -> null
    }

    /**
     * Constructs a [CredentialsProvider] from [GcpBuildServiceParams.credentialSource] and its
     * supporting fields, or returns `null` when [GcpBuildServiceParams.credentialSource] is unset.
     *
     * | [GcpCredentialSource] | Result |
     * |---|---|
     * | `NONE` | [NoCredentialsProvider.create] |
     * | unset | `null` |
     * | otherwise | [FixedCredentialsProvider.create] over [resolveCredentials] |
     */
    protected fun resolveCredentialsProvider(): CredentialsProvider? =
        when (parameters.credentialSource.orNull) {
            GcpCredentialSource.NONE -> NoCredentialsProvider.create()
            null -> null
            else -> resolveCredentials()?.let(FixedCredentialsProvider::create)
        }
}
