package com.kelvsyc.gradle.google.cloud.run

import com.google.cloud.run.v2.Container
import com.google.cloud.run.v2.EnvVar
import com.google.cloud.run.v2.Job
import com.google.cloud.run.v2.UpdateJobRequest
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that updates an existing Cloud Run Job definition.
 *
 * The action fetches the existing job, then updates its container image and environment variables.
 * The container is modified on the job's execution template's task template.
 *
 * This action blocks until the long-running operation completes.
 */
abstract class UpdateJobAction : WorkAction<UpdateJobAction.Parameters> {

    /**
     * Parameters for [UpdateJobAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The build service managing the Cloud Run Jobs client.
         */
        @get:Internal
        val service: Property<CloudRunJobsClientBuildService>

        /**
         * The full resource name of the job to update,
         * e.g. `projects/my-project/locations/us-central1/jobs/my-job`.
         */
        val jobName: Property<String>

        /**
         * The container image URI to deploy, e.g. `gcr.io/my-project/image:tag`.
         */
        val imageUri: Property<String>

        /**
         * Environment variables to set on the container.
         */
        val envVars: MapProperty<String, String>
    }

    override fun execute() {
        val client = parameters.service.get().getClient()
        val jobName = parameters.jobName.get()
        val imageUri = parameters.imageUri.get()
        val envVarsMap = parameters.envVars.get()

        // Fetch the existing job
        val existingJob = client.getJob(jobName)

        // Build the container with image and env vars
        val containerBuilder = Container.newBuilder()
            .setImage(imageUri)
        for ((key, value) in envVarsMap) {
            containerBuilder.addEnv(
                EnvVar.newBuilder()
                    .setName(key)
                    .setValue(value)
                    .build()
            )
        }

        // Update the job, replacing the container in the task template
        val updated = existingJob.toBuilder()
            .setTemplate(
                existingJob.template.toBuilder()
                    .setTemplate(
                        existingJob.template.template.toBuilder()
                            .clearContainers()
                            .addContainers(containerBuilder.build())
                            .build()
                    )
                    .build()
            )
            .build()

        val request = UpdateJobRequest.newBuilder()
            .setJob(updated)
            .build()

        client.updateJobAsync(request).get()
    }
}
