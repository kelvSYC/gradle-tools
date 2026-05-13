package com.kelvsyc.gradle.aws.java

import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.provider.Provider
import org.gradle.api.credentials.AwsCredentials as GradleAwsCredentials

/**
 * Configures these parameters to use
 * [AnonymousCredentialsProvider][software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider].
 */
fun AwsBuildServiceParams.anonymous() {
    credentialSource.set(AwsCredentialSource.ANONYMOUS)
}

/**
 * Configures these parameters to use
 * [DefaultCredentialsProvider][software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider],
 * which resolves credentials from environment variables, `~/.aws/credentials`, EC2/ECS/EKS
 * instance metadata, and other standard sources.
 */
fun AwsBuildServiceParams.defaultCredentials() {
    credentialSource.set(AwsCredentialSource.DEFAULT_CHAIN)
}

/**
 * Configures these parameters to use a
 * [StaticCredentialsProvider][software.amazon.awssdk.auth.credentials.StaticCredentialsProvider]
 * with [AwsBasicCredentials][software.amazon.awssdk.auth.credentials.AwsBasicCredentials].
 */
fun AwsBuildServiceParams.staticCredentials(accessKey: Provider<String>, secretKey: Provider<String>) {
    credentialSource.set(AwsCredentialSource.STATIC)
    accessKeyId.set(accessKey)
    secretAccessKey.set(secretKey)
}

/**
 * Configures these parameters to use a
 * [StaticCredentialsProvider][software.amazon.awssdk.auth.credentials.StaticCredentialsProvider]
 * with [AwsSessionCredentials][software.amazon.awssdk.auth.credentials.AwsSessionCredentials].
 */
fun AwsBuildServiceParams.sessionCredentials(
    accessKey: Provider<String>,
    secretKey: Provider<String>,
    token: Provider<String>,
) {
    credentialSource.set(AwsCredentialSource.STATIC)
    accessKeyId.set(accessKey)
    secretAccessKey.set(secretKey)
    sessionToken.set(token)
}

/**
 * Configures these parameters to use a
 * [ProfileCredentialsProvider][software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider]
 * for the named profile.
 */
fun AwsBuildServiceParams.profileCredentials(profile: String) {
    credentialSource.set(AwsCredentialSource.PROFILE)
    credentialsProfile.set(profile)
}

/**
 * Configures these parameters to use a
 * [StaticCredentialsProvider][software.amazon.awssdk.auth.credentials.StaticCredentialsProvider]
 * sourced from Gradle [PasswordCredentials], mapping [PasswordCredentials.getUsername] to the
 * access key ID and [PasswordCredentials.getPassword] to the secret access key.
 */
@JvmName("fromPasswordCredentials")
fun AwsBuildServiceParams.from(credentials: Provider<PasswordCredentials>) {
    credentialSource.set(AwsCredentialSource.STATIC)
    accessKeyId.set(credentials.map { it.username })
    secretAccessKey.set(credentials.map { it.password })
}

/**
 * Configures these parameters to use a
 * [StaticCredentialsProvider][software.amazon.awssdk.auth.credentials.StaticCredentialsProvider]
 * sourced from Gradle [AwsCredentials][GradleAwsCredentials]. When
 * [AwsCredentials.getSessionToken][GradleAwsCredentials.getSessionToken] is non-null,
 * [AwsSessionCredentials][software.amazon.awssdk.auth.credentials.AwsSessionCredentials] are
 * used; otherwise [AwsBasicCredentials][software.amazon.awssdk.auth.credentials.AwsBasicCredentials].
 */
@JvmName("fromAwsCredentials")
fun AwsBuildServiceParams.from(credentials: Provider<GradleAwsCredentials>) {
    credentialSource.set(AwsCredentialSource.STATIC)
    accessKeyId.set(credentials.map { it.accessKey })
    secretAccessKey.set(credentials.map { it.secretKey })
    sessionToken.set(credentials.map { it.sessionToken ?: "" }.filter { it.isNotEmpty() })
}
