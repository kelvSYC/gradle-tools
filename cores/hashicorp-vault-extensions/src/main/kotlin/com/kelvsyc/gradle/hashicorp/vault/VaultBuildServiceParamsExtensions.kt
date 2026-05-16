package com.kelvsyc.gradle.hashicorp.vault

import com.kelvsyc.gradle.clients.CredentialReference

/**
 * Configures token-based authentication using a pre-issued Vault token.
 *
 * @param token A reference to the Vault token. Defaults to the `VAULT_TOKEN` environment variable.
 */
fun VaultBuildServiceParams.tokenAuth(
    token: CredentialReference = CredentialReference.EnvironmentVariable("VAULT_TOKEN"),
) {
    credentialSource.set(VaultCredentialSource.TOKEN)
    tokenRef.set(token)
}

/**
 * Configures AppRole authentication.
 *
 * @param roleId The AppRole role ID (non-sensitive).
 * @param secretId A reference to the AppRole secret ID. Defaults to the `VAULT_SECRET_ID` environment variable.
 */
fun VaultBuildServiceParams.appRoleAuth(
    roleId: String,
    secretId: CredentialReference = CredentialReference.EnvironmentVariable("VAULT_SECRET_ID"),
) {
    credentialSource.set(VaultCredentialSource.APP_ROLE)
    this.roleId.set(roleId)
    secretIdRef.set(secretId)
}

/**
 * Configures Kubernetes service account authentication.
 *
 * @param role The Kubernetes auth role name configured in Vault.
 * @param jwtPath Path to the Kubernetes service account JWT file.
 *   Defaults to the standard in-cluster path `/var/run/secrets/kubernetes.io/serviceaccount/token`.
 */
fun VaultBuildServiceParams.kubernetesAuth(
    role: String,
    jwtPath: String = "/var/run/secrets/kubernetes.io/serviceaccount/token",
) {
    credentialSource.set(VaultCredentialSource.KUBERNETES)
    kubernetesRole.set(role)
    kubernetesJwtPath.set(jwtPath)
}

/**
 * Configures AWS IAM authentication. Vault will verify the caller's AWS identity
 * using the IAM credentials available in the environment.
 */
fun VaultBuildServiceParams.awsIamAuth() {
    credentialSource.set(VaultCredentialSource.AWS_IAM)
}

/**
 * Configures Google Cloud service account authentication. Vault will verify the
 * caller's GCP identity using the credentials available in the environment.
 *
 * @param jwt A reference to the GCP JWT. Defaults to the `GOOGLE_OAUTH2_TOKEN` environment variable.
 */
fun VaultBuildServiceParams.gcpAuth(
    jwt: CredentialReference = CredentialReference.EnvironmentVariable("GOOGLE_OAUTH2_TOKEN"),
) {
    credentialSource.set(VaultCredentialSource.GCP)
    jwtRef.set(jwt)
}

/**
 * Configures Azure Managed Identity authentication.
 *
 * **Note:** Azure Managed Identity authentication is not currently supported by
 * the underlying vault-java-driver client. Calling this method will register
 * [VaultCredentialSource.AZURE_MSI] as the credential source, but attempting
 * to create a Vault client will throw an [UnsupportedOperationException] at
 * build execution time.
 */
fun VaultBuildServiceParams.azureMsiAuth() {
    credentialSource.set(VaultCredentialSource.AZURE_MSI)
}
