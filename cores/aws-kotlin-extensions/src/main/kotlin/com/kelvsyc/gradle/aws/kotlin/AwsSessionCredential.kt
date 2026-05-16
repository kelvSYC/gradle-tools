package com.kelvsyc.gradle.aws.kotlin

import java.time.Instant

/**
 * Temporary AWS session credentials issued by the AWS Security Token Service (STS) via an `AssumeRole` call.
 *
 * Instances of this class are produced at **task execution time** by [AbstractAssumeRoleWorkAction] subclasses
 * and passed to [AbstractAssumeRoleWorkAction.doExecute]. They must be consumed entirely within the
 * `doExecute` body — typically by constructing a short-lived AWS SDK client with a
 * `StaticCredentialsProvider` backed by these values.
 *
 * ## Configuration cache safety
 *
 * This credential must not escape the body of `doExecute()`. The ways it can leak:
 *
 * - **WorkParameters property** (even `@get:Internal`): Gradle serializes all WorkParameters to
 *   `.gradle/configuration-cache/` in plaintext.
 * - **Task input, output, or property**: same serialization path.
 * - **Shared file or static field**: the value persists on disk beyond this build invocation.
 *
 * The correct pattern is to create and close an SDK client within the same `doExecute` call:
 *
 * ```kotlin
 * override fun doExecute(credential: AwsSessionCredential) {
 *     KmsClient { credentialsProvider = StaticCredentialsProvider {
 *         accessKeyId = credential.accessKeyId
 *         secretAccessKey = credential.secretAccessKey
 *         sessionToken = credential.sessionToken
 *     }}.use { client -> /* perform operation */ }
 * }
 * ```
 *
 * ## No explicit revocation
 *
 * Unlike Vault dynamic credentials, STS session credentials have no explicit revocation API.
 * They self-expire at [expiration]. There is no `revokeSession` equivalent that can be called
 * to invalidate them early. The credential is valid until its natural expiry regardless of whether
 * `doExecute` returns normally or throws.
 */
data class AwsSessionCredential(
    /** The AWS access key ID. */
    val accessKeyId: String,
    /** The AWS secret access key. */
    val secretAccessKey: String,
    /** The session token required for temporary-credential API calls. */
    val sessionToken: String,
    /** The instant at which this credential expires. */
    val expiration: Instant,
)
