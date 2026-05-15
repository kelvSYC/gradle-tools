package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.clients.CredentialReference

/**
 * Configures Bearer-token authentication with a [CredentialReference] for the token.
 *
 * [token] is a [CredentialReference] pointing to an environment variable or system property whose
 * value is the Bitbucket Data Center personal access token (or HTTP access token) — by default
 * `BITBUCKET_TOKEN`. The credential value is resolved at build execution time and never enters the
 * Gradle configuration cache.
 */
fun BitbucketServerClientBuildService.Params.bearerToken(
    token: CredentialReference = CredentialReference.EnvironmentVariable("BITBUCKET_TOKEN"),
) {
    this.tokenRef.set(token)
}
