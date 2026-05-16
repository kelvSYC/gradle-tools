package com.kelvsyc.gradle.aws.kotlin.sts

import aws.sdk.kotlin.services.sts.model.AssumeRoleRequest
import com.kelvsyc.gradle.aws.kotlin.AwsSessionCredential
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that assumes an AWS IAM role and produces an [AwsSessionCredential].
 *
 * Subclasses must implement [doExecute] to consume the resulting temporary credentials. The credential
 * must not outlive [doExecute]: storing it in a WorkParameters property (even `@get:Internal`), a task
 * input, or a shared file writes it to `.gradle/configuration-cache/` or disk in plaintext.
 *
 * This implementation wraps the AWS SDK for Kotlin coroutine API via [runBlocking].
 */
abstract class AbstractAssumeRoleWorkAction :
    WorkAction<AbstractAssumeRoleWorkAction.Parameters> {

    /**
     * Parameters for [AbstractAssumeRoleWorkAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The build service managing the STS client.
         */
        @get:Internal
        val service: Property<StsClientBuildService>

        /**
         * The ARN of the IAM role to assume (e.g., `arn:aws:iam::123456789012:role/MyRole`).
         */
        val roleArn: Property<String>

        /**
         * The name of the session when assuming the role (e.g., `my-build-session`).
         * AWS uses this to trace API calls back to the assumed role session.
         */
        val roleSessionName: Property<String>

        /**
         * The duration in seconds for which the assumed-role session is valid (900-3600 seconds;
         * default 3600 if not specified).
         */
        val duration: Property<Long>

        /**
         * An optional external ID required by the role's trust policy.
         * Leave unset if the role does not require one.
         */
        @get:Internal
        val externalId: Property<String>
    }

    final override fun execute() {
        val creds = runBlocking {
            val request = AssumeRoleRequest {
                roleArn = parameters.roleArn.get()
                roleSessionName = parameters.roleSessionName.get()
                durationSeconds = parameters.duration.get().toInt()
                if (parameters.externalId.isPresent) externalId = parameters.externalId.get()
            }
            parameters.service.get().getClient().assumeRole(request).credentials
                ?: error("No credentials returned by STS AssumeRole")
        }
        val credential = AwsSessionCredential(
            accessKeyId = creds.accessKeyId,
            secretAccessKey = creds.secretAccessKey,
            sessionToken = creds.sessionToken,
            expiration = Instant.ofEpochSecond(creds.expiration.epochSeconds, creds.expiration.nanosecondsOfSecond.toLong()),
        )
        doExecute(credential)
    }

    /**
     * Consumes the temporary session credential produced by [execute].
     *
     * This method is called at task execution time with the credential freshly obtained from the STS
     * AssumeRole call. The credential must be used entirely within this method — typically to construct
     * an AWS SDK client with a `StaticCredentialsProvider`.
     *
     * The credential must not escape this method. See [AwsSessionCredential] for the specific leak
     * vectors (WorkParameters, task inputs, shared files) and revocation semantics.
     */
    protected abstract fun doExecute(credential: AwsSessionCredential)
}
