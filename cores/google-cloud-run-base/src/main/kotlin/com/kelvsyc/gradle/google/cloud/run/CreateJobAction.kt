package com.kelvsyc.gradle.google.cloud.run

import com.google.cloud.run.v2.Container
import com.google.cloud.run.v2.EnvVar
import com.google.cloud.run.v2.ExecutionTemplate
import com.google.cloud.run.v2.Job
import com.google.cloud.run.v2.TaskTemplate
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that creates a new Cloud Run Job definition.
 *
 * The action parses the job ID and parent location from the full resource name,
 * then creates a new job with the specified container image and environment variables.
 * The container is set on the job's execution template.
 *
 * This action blocks until the long-running operation completes.
 */
abstract class CreateJobAction : WorkAction<CreateJobAction.Parameters> {

    /**
     * Parameters for [CreateJobAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The build service managing the Cloud Run Jobs client.
         */
        @get:Internal
        val service: Property<CloudRunJobsClientBuildService>

        /**
         * The full resource name of the job to create,
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

        // Extract job ID and parent from the full resource name
        val jobId = jobName.substringAfterLast("/")
        val parent = jobName.substringBeforeLast("/jobs/")

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

        // Build the job with execution template -> task template -> containers
        val job = Job.newBuilder()
            .setTemplate(
                ExecutionTemplate.newBuilder()
                    .setTemplate(
                        TaskTemplate.newBuilder()
                            .addContainers(containerBuilder.build())
                            .build()
                    )
                    .build()
            )
            .build()

        client.createJobAsync(parent, job, jobId).get()
    }
}
