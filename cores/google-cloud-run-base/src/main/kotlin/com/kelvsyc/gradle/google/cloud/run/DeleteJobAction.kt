package com.kelvsyc.gradle.google.cloud.run

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that deletes a Cloud Run Job definition.
 *
 * The action deletes the job identified by the full resource name.
 *
 * This action blocks until the long-running operation completes.
 */
abstract class DeleteJobAction : WorkAction<DeleteJobAction.Parameters> {

    /**
     * Parameters for [DeleteJobAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The build service managing the Cloud Run Jobs client.
         */
        @get:Internal
        val service: Property<CloudRunJobsClientBuildService>

        /**
         * The full resource name of the job to delete,
         * e.g. `projects/my-project/locations/us-central1/jobs/my-job`.
         */
        val jobName: Property<String>
    }

    override fun execute() {
        val client = parameters.service.get().getClient()
        client.deleteJobAsync(parameters.jobName.get()).get()
    }
}
