package com.kelvsyc.gradle.aws.java.sts

import com.kelvsyc.gradle.aws.java.AwsSessionCredential
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest

/**
 * Abstract WorkAction base class for assuming an IAM role via AWS STS and executing a task with the
 * resulting temporary credentials.
 *
 * Subclasses must implement [doExecute] to consume the temporary [AwsSessionCredential].
 *
 * ## Why `execute()` is final
 *
 * The `execute()` method is final to enforce the lifecycle contract: the [AwsSessionCredential]
 * is **only valid during task execution time** and must be consumed entirely within the `doExecute`
 * call. Subclasses cannot override `execute()` and risk storing or serializing the credential.
 *
 * ## What `doExecute` receives
 *
 * The `doExecute` method receives an [AwsSessionCredential] containing:
 * - [AwsSessionCredential.accessKeyId] — the AWS access key ID
 * - [AwsSessionCredential.secretAccessKey] — the secret access key
 * - [AwsSessionCredential.sessionToken] — the STS session token
 * - [AwsSessionCredential.expiration] — the instant at which the credential expires
 *
 * ## Critical: credential must not escape doExecute
 *
 * The credential must be consumed **entirely within `doExecute`**. The ways it can leak:
 *
 * - **WorkParameters property** (even `@get:Internal`): Gradle serializes all WorkParameters to
 *   `.gradle/configuration-cache/` in plaintext.
 * - **Task input, output, or property**: same serialization path.
 * - **Shared file or static field**: the value persists on disk beyond this build invocation.
 *
 * The correct pattern is to create a short-lived AWS SDK client using
 * `StaticCredentialsProvider.create(AwsSessionCredentials.create(...))` and call `.use { ... }`
 * on it to ensure proper resource cleanup:
 *
 * ```kotlin
 * override fun doExecute(credential: AwsSessionCredential) {
 *     KmsClient.builder()
 *         .credentialsProvider(StaticCredentialsProvider.create(
 *             AwsSessionCredentials.create(
 *                 credential.accessKeyId,
 *                 credential.secretAccessKey,
 *                 credential.sessionToken,
 *             )
 *         ))
 *         .build()
 *         .use { client -> /* perform KMS operation */ }
 * }
 * ```
 *
 * ## No explicit revocation
 *
 * Unlike Vault dynamic credentials, STS session credentials have no explicit revocation API.
 * They self-expire at [AwsSessionCredential.expiration]. There is no `revokeSession` equivalent
 * that can be called to invalidate them early. The credential is valid until its natural expiry
 * regardless of whether `doExecute` returns normally or throws.
 */
abstract class AbstractAssumeRoleWorkAction :
    WorkAction<AbstractAssumeRoleWorkAction.Parameters> {

    /**
     * WorkParameters for [AbstractAssumeRoleWorkAction].
     *
     * All fields except [service] and [externalId] contribute to task up-to-date checks.
     */
    interface Parameters : WorkParameters {
        /**
         * The [StsClientBuildService] instance to use for the assume-role API call.
         * Excluded from task snapshots to avoid serializing the underlying AWS SDK client.
         */
        @get:Internal
        val service: Property<StsClientBuildService>

        /**
         * The ARN of the IAM role to assume.
         * Contributes to task up-to-date checks.
         */
        val roleArn: Property<String>

        /**
         * The name to tag this session with (used in CloudTrail and other AWS logs).
         * Contributes to task up-to-date checks.
         */
        val roleSessionName: Property<String>

        /**
         * The duration of the temporary credential in seconds.
         * STS enforces a minimum of 900 seconds (15 minutes) and a maximum that varies by role.
         * Contributes to task up-to-date checks.
         */
        val duration: Property<Long>

        /**
         * The external ID for cross-account trust (optional).
         * If the role's trust policy requires an external ID, set it here.
         * Excluded from task snapshots to prevent logging sensitive values.
         */
        @get:Internal
        val externalId: Property<String>
    }

    final override fun execute() {
        val request = AssumeRoleRequest.builder().apply {
            roleArn(parameters.roleArn.get())
            roleSessionName(parameters.roleSessionName.get())
            durationSeconds(parameters.duration.get().toInt())
            if (parameters.externalId.isPresent) externalId(parameters.externalId.get())
        }.build()
        val creds = parameters.service.get().getClient().assumeRole(request).credentials()
        val credential = AwsSessionCredential(
            accessKeyId = creds.accessKeyId(),
            secretAccessKey = creds.secretAccessKey(),
            sessionToken = creds.sessionToken(),
            expiration = creds.expiration(),
        )
        doExecute(credential)
    }

    /**
     * Executes the task with the temporary AWS credentials obtained from the assume-role call.
     *
     * @param credential The temporary AWS credentials. Must be consumed entirely within this call;
     * typically used to construct a short-lived AWS SDK client with a `StaticCredentialsProvider`.
     */
    protected abstract fun doExecute(credential: AwsSessionCredential)
}
