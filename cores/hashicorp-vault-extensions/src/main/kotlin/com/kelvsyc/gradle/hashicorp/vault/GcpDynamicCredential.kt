package com.kelvsyc.gradle.hashicorp.vault

import java.time.Duration

/**
 * Dynamic GCP credentials issued by Vault's GCP secrets engine.
 *
 * @property token The OAuth2 access token or service account key.
 * @property leaseId The Vault lease ID. Used to renew or revoke this credential.
 * @property leaseDuration The duration for which this credential is valid.
 */
data class GcpDynamicCredential(
    val token: String,
    val leaseId: String,
    val leaseDuration: Duration,
)
