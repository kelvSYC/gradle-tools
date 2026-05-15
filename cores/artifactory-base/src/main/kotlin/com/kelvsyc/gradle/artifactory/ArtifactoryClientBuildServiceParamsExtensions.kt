package com.kelvsyc.gradle.artifactory

import com.kelvsyc.gradle.clients.CredentialReference

/**
 * Configures anonymous (unauthenticated) access. This is the default when [ArtifactoryClientBuildService.Params.passwordRef]
 * is not set.
 */
fun ArtifactoryClientBuildService.Params.anonymous() {
    // no-op: anonymous is the default when passwordRef is unset
}

/**
 * Configures basic-auth access with a fixed [username] and a [CredentialReference] for the
 * password or API token.
 *
 * [username] is a non-sensitive identifier stored as a plain string. [password] is a
 * [CredentialReference] pointing to an environment variable or system property whose value is
 * the password or API token — by default `ARTIFACTORY_PASSWORD`. The credential value is resolved
 * at build execution time and never enters the Gradle configuration cache.
 */
fun ArtifactoryClientBuildService.Params.basicAuth(
    username: String,
    password: CredentialReference = CredentialReference.EnvironmentVariable("ARTIFACTORY_PASSWORD"),
) {
    this.username.set(username)
    this.passwordRef.set(password)
}
