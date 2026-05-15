package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.clients.CredentialReference

/**
 * Configures basic-auth access with a fixed [username] and a [CredentialReference] for the app
 * password.
 *
 * [username] is a non-sensitive identifier stored as a plain string. [password] is a
 * [CredentialReference] pointing to an environment variable or system property whose value is the
 * Bitbucket Cloud app password — by default `BITBUCKET_APP_PASSWORD`. The credential value is
 * resolved at build execution time and never enters the Gradle configuration cache.
 */
fun BitbucketCloudClientBuildService.Params.basicAuth(
    username: String,
    password: CredentialReference = CredentialReference.EnvironmentVariable("BITBUCKET_APP_PASSWORD"),
) {
    this.username.set(username)
    this.passwordRef.set(password)
}
