package com.kelvsyc.gradle.azure.containerapp.actions

import com.azure.resourcemanager.appcontainers.models.EnvironmentVar
import com.kelvsyc.gradle.azure.containerapp.ContainerAppBuildService
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that updates the container image (and optionally environment variables) on an
 * existing Container App's first container, triggering a new revision.
 *
 * Only the first container in the app template is updated. Sidecars are preserved unchanged.
 * All other app settings (ingress, secrets, scale rules) are preserved.
 */
abstract class UpdateContainerAppAction : WorkAction<UpdateContainerAppAction.Parameters> {

    companion object {
        private const val PRIMARY_CONTAINER_INDEX = 0
    }

    /**
     * Parameters for [UpdateContainerAppAction].
     */
    interface Parameters : WorkParameters {
        /** The scoped service providing the target [com.azure.resourcemanager.appcontainers.models.ContainerApp]. */
        @get:Internal
        val service: Property<ContainerAppBuildService>

        /** New container image URI to deploy. */
        val imageUri: Property<String>

        /** Replacement environment variables. When absent, existing env vars are preserved. */
        val envVars: MapProperty<String, String>
    }

    override fun execute() {
        val app = parameters.service.get().getClient()
        val currentTemplate = app.template() ?: return
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

        app.update()
            .withTemplate(currentTemplate.withContainers(updatedContainers))
            .apply()
    }
}
