package com.kelvsyc.gradle.azure.containerapp.actions

import com.azure.resourcemanager.appcontainers.models.EnvironmentVar
import com.kelvsyc.gradle.azure.containerapp.ContainerAppJobBuildService
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that updates the container image (and optionally environment variables) on the
 * first container of an existing Container App Job definition.
 *
 * Job configuration (trigger type, cron schedule, replica count) is preserved unchanged.
 */
abstract class UpdateContainerAppJobAction : WorkAction<UpdateContainerAppJobAction.Parameters> {

    /**
     * Parameters for [UpdateContainerAppJobAction].
     */
    interface Parameters : WorkParameters {
        /** The scoped service providing the target [com.azure.resourcemanager.appcontainers.models.Job]. */
        @get:Internal
        val service: Property<ContainerAppJobBuildService>

        /** New container image URI to set on the job. */
        val imageUri: Property<String>

        /** Replacement environment variables. When absent, existing env vars are preserved. */
        val envVars: MapProperty<String, String>
    }

    override fun execute() {
        val job = parameters.service.get().getClient()
        val currentTemplate = job.template() ?: return
        val containers = currentTemplate.containers() ?: return

        val imageUri = parameters.imageUri.get()
        val updatedContainers = containers.mapIndexed { index, container ->
            if (index == PRIMARY_CONTAINER_INDEX) {
                container.withImage(imageUri).also { c ->
                    if (parameters.envVars.isPresent) {
                        val envList = parameters.envVars.get().map { (k, v) ->
                            EnvironmentVar().withName(k).withValue(v)
                        }
                        c.withEnv(envList)
                    }
                }
            } else {
                container
            }
        }

        currentTemplate.withContainers(updatedContainers)
        job.update().apply()
    }

    private companion object {
        private const val PRIMARY_CONTAINER_INDEX = 0
    }
}
