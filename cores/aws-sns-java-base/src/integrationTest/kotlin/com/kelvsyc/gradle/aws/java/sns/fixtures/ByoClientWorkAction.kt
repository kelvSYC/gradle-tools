package com.kelvsyc.gradle.aws.java.sns.fixtures

import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.sns.SnsClient

/**
 * Variant B proposed BYO retrofit: a `WorkAction` whose `WorkParameters` directly holds the live SDK client
 * (`Property<SnsClient>`), mirroring the task-level BYO pattern used by AWS S3 / SNS batch tasks.
 *
 * The probe pinned to this action answers the question deferred in the plan: does `Property<LiveClient>`
 * survive `WorkerExecutor` submission serialization, or is the action-dispatch asymmetry structural?
 */
abstract class ByoClientWorkAction : WorkAction<ByoClientWorkAction.Parameters> {
    /** Parameters for [ByoClientWorkAction]. */
    interface Parameters : WorkParameters {
        /** The live SDK client under probe. */
        val client: Property<SnsClient>
    }

    override fun execute() {
        val client = checkNotNull(parameters.client.orNull) { "client property unexpectedly absent" }
        check(client::class.qualifiedName != null)
    }
}
