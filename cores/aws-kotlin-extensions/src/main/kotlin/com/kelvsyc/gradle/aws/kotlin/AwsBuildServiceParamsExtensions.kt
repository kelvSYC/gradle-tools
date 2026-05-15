package com.kelvsyc.gradle.aws.kotlin

import com.kelvsyc.gradle.clients.CredentialReference
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.provider.Provider
import org.gradle.api.credentials.AwsCredentials as GradleAwsCredentials

/**
 * Configures these parameters for anonymous mode. The build service will not assign a
 * `credentialsProvider` to the AWS Kotlin SDK client.
 */
fun AwsBuildServiceParams.anonymous() {
    credentialSource.set(AwsCredentialSource.ANONYMOUS)
}

/**
 * Configures these parameters to use
 * [DefaultChainCredentialsProvider][aws.sdk.kotlin.runtime.auth.credentials.DefaultChainCredentialsProvider],
 * which resolves credentials from environment variables, `~/.aws/credentials`, EC2/ECS/EKS
 * instance metadata, and other standard sources.
 */
fun AwsBuildServiceParams.defaultCredentials() {
    credentialSource.set(AwsCredentialSource.DEFAULT_CHAIN)
}

/**
 * Configures these parameters to use a
 * [StaticCredentialsProvider][aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider]
 * with [Credentials][aws.smithy.kotlin.runtime.auth.awscredentials.Credentials] built from the
 * supplied access key and secret key.
 *
 * By default, credentials are resolved from the standard AWS environment variables. Callers may
 * provide explicit [CredentialReference] instances to override this behavior.
 */
fun AwsBuildServiceParams.staticCredentials(
    accessKey: CredentialReference = CredentialReference.EnvironmentVariable("AWS_ACCESS_KEY_ID"),
    secretKey: CredentialReference = CredentialReference.EnvironmentVariable("AWS_SECRET_ACCESS_KEY"),
) {
    credentialSource.set(AwsCredentialSource.STATIC)
    accessKeyIdRef.set(accessKey)
    secretAccessKeyRef.set(secretKey)
}

/**
 * Configures these parameters to use a
 * [StaticCredentialsProvider][aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider]
 * with session [Credentials][aws.smithy.kotlin.runtime.auth.awscredentials.Credentials] built
 * from the supplied access key, secret key, and session token.
 *
 * By default, credentials are resolved from the standard AWS environment variables. Callers may
 * provide explicit [CredentialReference] instances to override this behavior.
 */
fun AwsBuildServiceParams.sessionCredentials(
    accessKey: CredentialReference = CredentialReference.EnvironmentVariable("AWS_ACCESS_KEY_ID"),
    secretKey: CredentialReference = CredentialReference.EnvironmentVariable("AWS_SECRET_ACCESS_KEY"),
    token: CredentialReference = CredentialReference.EnvironmentVariable("AWS_SESSION_TOKEN"),
) {
    credentialSource.set(AwsCredentialSource.STATIC)
    accessKeyIdRef.set(accessKey)
    secretAccessKeyRef.set(secretKey)
    sessionTokenRef.set(token)
}

/**
 * Configures these parameters to use a
 * [ProfileCredentialsProvider][aws.sdk.kotlin.runtime.auth.credentials.ProfileCredentialsProvider]
 * for the named profile.
 */
fun AwsBuildServiceParams.profileCredentials(profile: String) {
    credentialSource.set(AwsCredentialSource.PROFILE)
    credentialsProfile.set(profile)
}

/**
 * Configures these parameters to use a
 * [StaticCredentialsProvider][aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider]
 * sourced from Gradle [PasswordCredentials], mapping [PasswordCredentials.getUsername] to the
 * access key ID and [PasswordCredentials.getPassword] to the secret access key.
 *
 * **Deprecated — configuration cache unsafe.** [PasswordCredentials] is a configuration-time
 * construct designed for Gradle repository authentication (dependency resolution). It is not
 * intended to supply credentials to services that authenticate during task execution. Calling
 * this function resolves the provider during the configuration phase, which causes the actual
 * credential values to be stored in the Gradle configuration cache in plaintext.
 *
 * **Prefer** [staticCredentials] with [CredentialReference.EnvironmentVariable] for CI/CD, or
 * [CredentialReference.SystemProperty] for `gradle.properties`-backed credentials using the
 * `systemProp.` convention (see [CredentialReference.SystemProperty] for details).
 */
@Deprecated(
    "PasswordCredentials is a configuration-phase construct designed for repository auth, not " +
        "for build services that run at task execution time. Resolving it here stores the actual " +
        "credential values in the Gradle configuration cache. Prefer staticCredentials() with " +
        "CredentialReference.EnvironmentVariable or CredentialReference.SystemProperty instead.",
    level = DeprecationLevel.WARNING,
)
@JvmName("fromPasswordCredentials")
fun AwsBuildServiceParams.from(credentials: Provider<PasswordCredentials>) {
    credentialSource.set(AwsCredentialSource.STATIC)
    accessKeyIdRef.set(credentials.map { CredentialReference.Literal(it.username ?: "") }.get())
    secretAccessKeyRef.set(credentials.map { CredentialReference.Literal(it.password ?: "") }.get())
}

/**
 * Configures these parameters to use a
 * [StaticCredentialsProvider][aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider]
 * sourced from Gradle [AwsCredentials][GradleAwsCredentials]. When
 * [AwsCredentials.getSessionToken][GradleAwsCredentials.getSessionToken] is non-null, session
 * credentials are used; otherwise basic credentials.
 *
 * **Deprecated — configuration cache unsafe.** [GradleAwsCredentials] is a configuration-time
 * construct designed for Gradle repository authentication (dependency resolution). It is not
 * intended to supply credentials to services that authenticate during task execution. Calling
 * this function resolves the provider during the configuration phase, which causes the actual
 * credential values to be stored in the Gradle configuration cache in plaintext.
 *
 * **Prefer** [staticCredentials] or [sessionCredentials] with [CredentialReference.EnvironmentVariable]
 * for CI/CD, or [CredentialReference.SystemProperty] for `gradle.properties`-backed credentials
 * using the `systemProp.` convention (see [CredentialReference.SystemProperty] for details).
 */
@Deprecated(
    "AwsCredentials is a configuration-phase construct designed for repository auth, not " +
        "for build services that run at task execution time. Resolving it here stores the actual " +
        "credential values in the Gradle configuration cache. Prefer staticCredentials() or " +
        "sessionCredentials() with CredentialReference.EnvironmentVariable or " +
        "CredentialReference.SystemProperty instead.",
    level = DeprecationLevel.WARNING,
)
@JvmName("fromAwsCredentials")
fun AwsBuildServiceParams.from(credentials: Provider<GradleAwsCredentials>) {
    credentialSource.set(AwsCredentialSource.STATIC)
    val creds = credentials.get()
    accessKeyIdRef.set(CredentialReference.Literal(creds.accessKey ?: ""))
    secretAccessKeyRef.set(CredentialReference.Literal(creds.secretKey ?: ""))
    val token = creds.sessionToken
    if (token != null && token.isNotEmpty()) {
        sessionTokenRef.set(CredentialReference.Literal(token))
    }
}
