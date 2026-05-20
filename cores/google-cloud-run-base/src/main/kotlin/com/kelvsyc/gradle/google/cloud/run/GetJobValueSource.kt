package com.kelvsyc.gradle.google.cloud.run

import com.google.api.gax.rpc.NotFoundException
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing the latest created execution name of a Cloud Run job.
 *
 * Returns `null` if the job does not exist or has no latest execution.
 *
 * The [Parameters.jobName] must be the full resource name in the form
 * `projects/{project}/locations/{location}/jobs/{job}`.
 */
abstract class GetJobValueSource : ValueSource<String, GetJobValueSource.Parameters> {

    /**
     * Parameters for [GetJobValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the Cloud Run Jobs client.
         */
        @get:Internal
        val service: Property<CloudRunJobsClientBuildService>

        /**
         * The full resource name of the job, e.g.
         * `projects/my-project/locations/us-central1/jobs/my-job`.
         */
        val jobName: Property<String>
    }

    override fun obtain(): String? {
        return try {
            val job = parameters.service.get().getClient().getJob(parameters.jobName.get())
            job.latestCreatedExecution.name.takeIf { it.isNotBlank() }
        } catch (@Suppress("SwallowedException") e: NotFoundException) {
            null
        }
    }
}
