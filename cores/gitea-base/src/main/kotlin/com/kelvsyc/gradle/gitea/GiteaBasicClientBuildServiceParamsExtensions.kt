package com.kelvsyc.gradle.gitea

import com.kelvsyc.gradle.clients.CredentialReference

/**
 * Configures basic-auth access with a fixed [username] and a [CredentialReference] for the password.
 *
 * [username] is a non-sensitive identifier stored as a plain string. [password] is a
 * [CredentialReference] pointing to an environment variable or system property whose value is the
 * Gitea password — by default `GITEA_PASSWORD`. The credential value is resolved at build execution
 * time and never enters the Gradle configuration cache.
 */
fun GiteaBasicClientBuildService.Params.basicAuth(
    username: String,
    password: CredentialReference = CredentialReference.EnvironmentVariable("GITEA_PASSWORD"),
) {
    this.username.set(username)
    this.passwordRef.set(password)
}
