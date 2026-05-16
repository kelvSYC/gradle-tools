package com.kelvsyc.gradle.hashicorp.vault

import java.time.Duration

/**
 * Dynamic AWS credentials issued by Vault's AWS secrets engine.
 *
 * @property accessKeyId The AWS access key ID.
 * @property secretAccessKey The AWS secret access key.
 * @property sessionToken The AWS session token, present for STS-based credentials.
 * @property leaseId The Vault lease ID. Used to renew or revoke this credential.
 * @property leaseDuration The duration for which this credential is valid.
 */
data class AwsDynamicCredential(
    val accessKeyId: String,
    val secretAccessKey: String,
    val sessionToken: String?,
    val leaseId: String,
    val leaseDuration: Duration,
)
