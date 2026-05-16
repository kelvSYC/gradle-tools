package com.kelvsyc.gradle.nexus

import com.kelvsyc.gradle.clients.CredentialReference

/**
 * Configures anonymous (unauthenticated) access. This is the default when
 * [NexusClientBuildService.Params.username] is not set.
 */
fun NexusClientBuildService.Params.anonymous() {
    // no-op: anonymous is the default when username is unset
}

/**
 * Configures basic-auth access with a fixed [username] and a [CredentialReference] for the
 * password.
 *
 * [username] is a non-sensitive identifier stored as a plain string. [password] is a
 * [CredentialReference] pointing to an environment variable or system property whose value is
 * the password — by default `NEXUS_PASSWORD`. The credential value is resolved at build execution
 * time and never enters the Gradle configuration cache.
 *
 * Nexus user tokens (generated via the Nexus UI) are presented as a username/password pair and
 * can be supplied here without API difference.
 */
fun NexusClientBuildService.Params.basicAuth(
    username: String,
    password: CredentialReference = CredentialReference.EnvironmentVariable("NEXUS_PASSWORD"),
) {
    this.username.set(username)
    this.passwordRef.set(password)
}
