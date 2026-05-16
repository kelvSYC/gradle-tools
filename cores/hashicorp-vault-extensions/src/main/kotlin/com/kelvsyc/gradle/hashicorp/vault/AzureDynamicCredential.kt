package com.kelvsyc.gradle.hashicorp.vault

import java.time.Duration

/**
 * Dynamic Azure credentials issued by Vault's Azure secrets engine.
 *
 * @property clientId The Azure client ID.
 * @property clientSecret The Azure client secret.
 * @property leaseId The Vault lease ID. Used to renew or revoke this credential.
 * @property leaseDuration The duration for which this credential is valid.
 */
data class AzureDynamicCredential(
    val clientId: String,
    val clientSecret: String,
    val leaseId: String,
    val leaseDuration: Duration,
)
