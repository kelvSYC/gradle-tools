package com.kelvsyc.gradle.aws.kotlin.sts

import aws.sdk.kotlin.services.sts.model.AssumeRoleRequest
import com.kelvsyc.gradle.aws.kotlin.AwsSessionCredential
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.UntrackedTask

/**
 * [DefaultTask] implementation that assumes an AWS IAM role and produces an [AwsSessionCredential].
 *
 * The task calls AWS STS AssumeRole to obtain temporary credentials valid for the configured duration.
 * Subclasses must implement [doExecute] to consume the credential at execution time.
 *
 * **Credential Lifecycle:**
 * The credential obtained by [execute] is temporary and self-expires after the configured `duration`.
 * AWS STS does not expose an explicit credential revocation API; once the session expires, the
 * credentials become unusable. The credential must be used entirely within the [doExecute] call to
 * construct a short-lived AWS SDK client and must not be stored in any Gradle property, task input,
 * or shared file — doing so would write the credential to `.gradle/configuration-cache/` or disk
 * in plaintext and violate configuration cache security guarantees.
 *
 * **Config Cache Safety:**
 * The credential must not escape the [doExecute] method. Storing it in a task input, a `Property`,
 * or a shared file is unsafe. See [AwsSessionCredential] for the complete list of leak vectors.
 *
 * **Security-Sensitive Properties:**
 * The [externalId] property is marked `@get:Internal` because it is security-sensitive and should not
 * appear in task snapshots (which are logged and cached). The other properties are `@get:Input` as
 * they do not contain secrets.
 *
 * **Example:**
 * ```kotlin
 * abstract class MyAssumeRole : AbstractAssumeRole() {
 *     override fun doExecute(credential: AwsSessionCredential) {
 *         KmsClient {
 *             credentialsProvider = StaticCredentialsProvider {
 *                 accessKeyId = credential.accessKeyId
 *                 secretAccessKey = credential.secretAccessKey
 *                 sessionToken = credential.sessionToken
 *             }
 *         }.use { client ->
 *             client.describeKey(DescribeKeyRequest { keyId = "arn:aws:kms:us-east-1:..." })
 *         }
 *     }
 * }
 *
 * tasks.register<MyAssumeRole>("assumeRole") {
 *     service.set(sts)
 *     roleArn.set("arn:aws:iam::123456789012:role/MyRole")
 *     roleSessionName.set("my-build-session")
 *     duration.set(3600L)
 * }
 * ```
 */
@UntrackedTask(because = "Communicates with AWS STS; no local output")
abstract class AbstractAssumeRole : DefaultTask() {
    /**
     * The build service managing the STS client.
     */
    @get:Internal
    abstract val service: Property<StsClientBuildService>

    /**
     * The ARN of the IAM role to assume (e.g., `arn:aws:iam::123456789012:role/MyRole`).
     */
    @get:Input
    abstract val roleArn: Property<String>

    /**
     * The name of the session when assuming the role (e.g., `my-build-session`).
     * AWS uses this to trace API calls back to the assumed role session.
     */
    @get:Input
    abstract val roleSessionName: Property<String>

    /**
     * The duration in seconds for which the assumed-role session is valid (900-3600 seconds;
     * default 3600 if not specified).
     */
    @get:Input
    abstract val duration: Property<Long>

    /**
     * An optional external ID required by the role's trust policy.
     * Leave unset if the role does not require one.
     *
     * This property is marked `@get:Internal` because it is security-sensitive and should not
     * appear in task snapshots.
     */
    @get:Internal
    abstract val externalId: Property<String>

    /**
     * Executes the AssumeRole API call and passes the credential to [doExecute].
     *
     * This is the `@TaskAction` that runs when the task executes. It calls AWS STS AssumeRole with the
     * configured parameters, receives temporary credentials, and immediately passes them to [doExecute]
     * for consumption. The credential is not stored anywhere outside of [doExecute].
     */
    @TaskAction
    fun execute() {
        val creds = runBlocking {
            val request = AssumeRoleRequest {
                roleArn = this@AbstractAssumeRole.roleArn.get()
                roleSessionName = this@AbstractAssumeRole.roleSessionName.get()
                durationSeconds = this@AbstractAssumeRole.duration.get().toInt()
                if (this@AbstractAssumeRole.externalId.isPresent) {
                    externalId = this@AbstractAssumeRole.externalId.get()
                }
            }
            service.get().getClient().assumeRole(request).credentials
                ?: error("No credentials returned by STS AssumeRole")
        }
        val credential = AwsSessionCredential(
            accessKeyId = creds.accessKeyId,
            secretAccessKey = creds.secretAccessKey,
            sessionToken = creds.sessionToken,
            expiration = Instant.ofEpochSecond(
                creds.expiration.epochSeconds,
                creds.expiration.nanosecondsOfSecond.toLong(),
            ),
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
     * vectors (task inputs, shared files) and credential lifecycle semantics.
     *
     * @param credential The temporary session credential from the STS AssumeRole call, valid for the
     * configured [duration] seconds.
     */
    protected abstract fun doExecute(credential: AwsSessionCredential)
}
