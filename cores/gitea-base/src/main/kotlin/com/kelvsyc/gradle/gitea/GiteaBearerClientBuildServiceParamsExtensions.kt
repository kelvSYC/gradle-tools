package com.kelvsyc.gradle.gitea

import com.kelvsyc.gradle.clients.CredentialReference

/**
 * Configures bearer-token authentication with a [CredentialReference] for the token.
 *
 * [token] is a [CredentialReference] pointing to an environment variable or system property whose
 * value is the Gitea API access token — by default `GITEA_TOKEN`. The credential value is resolved
 * at build execution time and never enters the Gradle configuration cache.
 */
fun GiteaBearerClientBuildService.Params.bearerToken(
    token: CredentialReference = CredentialReference.EnvironmentVariable("GITEA_TOKEN"),
) {
    this.tokenRef.set(token)
}
