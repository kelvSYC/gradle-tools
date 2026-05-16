package com.kelvsyc.gradle.hashicorp.vault

import java.time.Duration

/**
 * Dynamic database credentials issued by Vault's database secrets engine.
 *
 * @property username The database username.
 * @property password The database password.
 * @property leaseId The Vault lease ID. Used to renew or revoke this credential.
 * @property leaseDuration The duration for which this credential is valid.
 */
data class DatabaseCredential(
    val username: String,
    val password: String,
    val leaseId: String,
    val leaseDuration: Duration,
)
