package com.kelvsyc.gradle.hashicorp.vault

import com.kelvsyc.gradle.clients.CredentialReference
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Parameters for configuring a HashiCorp Vault build service.
 *
 * Sensitive credential fields use [CredentialReference] — only the lookup key
 * (environment variable name or system property name) is serialized to the
 * Gradle configuration cache, never the actual credential value.
 *
 * Non-sensitive fields (endpoint, namespace, role IDs, paths) are plain [Property] values.
 */
interface VaultBuildServiceParams : BuildServiceParameters {
    /** The Vault server endpoint, e.g. `https://vault.example.com:8200`. */
    val endpoint: Property<String>

    /**
     * The Vault namespace (Vault Enterprise only). Leave unset for open-source Vault.
     */
    val namespace: Property<String>

    /**
     * A CA certificate file for TLS verification. If unset, the JVM's default trust store is used.
     */
    val caCertFile: RegularFileProperty

    /**
     * Disables TLS certificate verification.
     *
     * **WARNING:** Never set this to `true` in production. It makes connections vulnerable
     * to man-in-the-middle attacks. Only use in controlled internal environments with
     * self-signed certificates.
     */
    val skipVerify: Property<Boolean>

    /** The authentication method to use when connecting to Vault. */
    val credentialSource: Property<VaultCredentialSource>

    /**
     * A reference to the Vault token for [VaultCredentialSource.TOKEN] authentication.
     * Resolved at build execution time — never stored as a plaintext value.
     */
    val tokenRef: Property<CredentialReference>

    /**
     * The AppRole role ID for [VaultCredentialSource.APP_ROLE] authentication.
     * The role ID is non-sensitive and may appear in task snapshots.
     */
    val roleId: Property<String>

    /**
     * A reference to the AppRole secret ID for [VaultCredentialSource.APP_ROLE] authentication.
     * Resolved at build execution time — never stored as a plaintext value.
     */
    val secretIdRef: Property<CredentialReference>

    /**
     * A reference to the JWT for [VaultCredentialSource.APP_ROLE] JWT or cloud IAM authentication.
     * Resolved at build execution time — never stored as a plaintext value.
     */
    val jwtRef: Property<CredentialReference>

    /**
     * The Kubernetes auth role name for [VaultCredentialSource.KUBERNETES] authentication.
     * The role name is non-sensitive.
     */
    val kubernetesRole: Property<String>

    /**
     * The filesystem path to the Kubernetes service account JWT file.
     * Defaults to the standard in-cluster path. The path itself is non-sensitive;
     * the file content is read at build execution time.
     */
    val kubernetesJwtPath: Property<String>
}
