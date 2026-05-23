package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.putSecretValue
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Task that stores a new secret value in an existing Secrets Manager secret.
 *
 * Only string secrets are supported. The secret must already exist — use `CreateSecret` via the AWS CLI
 * or console to create new secrets.
 */
@UntrackedTask(because = "Communicates with AWS Secrets Manager; no local output")
abstract class PutSecretValue : DefaultTask() {

    /** The build service managing the Secrets Manager client. */
    @get:Internal
    abstract val service: Property<SecretsManagerClientBuildService>

    /** The name or ARN of the secret to update. */
    @get:Input
    abstract val secretId: Property<String>

    /** The new secret value (string). */
    @get:Input
    abstract val secretString: Property<String>

    @TaskAction
    fun execute() = runBlocking {
        service.get().getClient().putSecretValue {
            secretId = this@PutSecretValue.secretId.get()
            secretString = this@PutSecretValue.secretString.get()
        }
    }
}
