package com.kelvsyc.gradle.hashicorp.vault

/**
 * The authentication method used to authenticate to HashiCorp Vault.
 */
enum class VaultCredentialSource {
    /** Authenticate using a pre-issued Vault token. */
    TOKEN,
    /** Authenticate using AppRole (role ID + secret ID). */
    APP_ROLE,
    /** Authenticate using AWS IAM credentials. */
    AWS_IAM,
    /** Authenticate using Google Cloud service account credentials. */
    GCP,
    /** Authenticate using Azure Managed Identity. */
    AZURE_MSI,
    /** Authenticate using a Kubernetes service account JWT. */
    KUBERNETES,
}
