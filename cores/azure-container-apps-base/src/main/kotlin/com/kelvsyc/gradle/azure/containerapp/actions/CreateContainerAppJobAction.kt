package com.kelvsyc.gradle.azure.containerapp.actions

import com.azure.resourcemanager.appcontainers.models.Container
import com.azure.resourcemanager.appcontainers.models.EnvironmentVar
import com.azure.resourcemanager.appcontainers.models.JobConfiguration
import com.azure.resourcemanager.appcontainers.models.JobConfigurationScheduleTriggerConfig
import com.azure.resourcemanager.appcontainers.models.JobTemplate
import com.azure.resourcemanager.appcontainers.models.TriggerType
import com.kelvsyc.gradle.azure.containerapp.ContainerAppsEnvironmentBuildService
import com.kelvsyc.gradle.azure.containerapp.JobTriggerType
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that creates a new Container App Job definition within the configured managed
 * environment.
 *
 * Always uses the environment service (not [com.kelvsyc.gradle.azure.containerapp.ContainerAppJobBuildService])
 * because the chained service's createClient calls `getByResourceGroup`, which would fail for a
 * job that does not yet exist.
 */
abstract class CreateContainerAppJobAction : WorkAction<CreateContainerAppJobAction.Parameters> {

    /**
     * Parameters for [CreateContainerAppJobAction].
     */
    interface Parameters : WorkParameters {
        /** The environment service. Resource group and environment name are read from its params. */
        @get:Internal
        val service: Property<ContainerAppsEnvironmentBuildService>

        /** Name of the new job. */
        val jobName: Property<String>

        /** Container image URI (e.g. `myregistry.azurecr.io/myjob:1.0`). */
        val imageUri: Property<String>

        /** Azure region (e.g. `eastus`). Must match the environment region. */
        val location: Property<String>

        /** How the job is triggered. */
        val triggerType: Property<JobTriggerType>

        /** Cron expression for [JobTriggerType.SCHEDULED] jobs (e.g. `0 * * * *`). */
        val cronExpression: Property<String>

        /** Number of job replicas to run per execution. Defaults to 1 when absent. */
        val replicaCompletionCount: Property<Int>

        /** Optional environment variables to set on the job container. */
        val envVars: MapProperty<String, String>
    }

    override fun execute() {
        val svc = parameters.service.get()
        val manager = svc.getClient()
        val rg = svc.parameters.resourceGroupName.get()
        val envName = svc.parameters.environmentName.get()
        val envId = manager.managedEnvironments().getByResourceGroup(rg, envName).id()

        val envList = parameters.envVars.getOrElse(emptyMap()).map { (k, v) ->
            EnvironmentVar().withName(k).withValue(v)
        }

        val container = Container()
            .withName("main")
            .withImage(parameters.imageUri.get())
            .withEnv(envList)

        val template = JobTemplate().withContainers(listOf(container))

        val sdkTriggerType = when (parameters.triggerType.get()) {
            JobTriggerType.MANUAL -> TriggerType.MANUAL
            JobTriggerType.SCHEDULED -> TriggerType.SCHEDULE
            JobTriggerType.EVENT -> TriggerType.EVENT
        }

        val configuration = JobConfiguration()
            .withTriggerType(sdkTriggerType)
            .also { config ->
                if (parameters.triggerType.get() == JobTriggerType.SCHEDULED) {
                    config.withScheduleTriggerConfig(
                        JobConfigurationScheduleTriggerConfig()
                            .withCronExpression(parameters.cronExpression.get())
                            .withReplicaCompletionCount(parameters.replicaCompletionCount.getOrElse(DEFAULT_REPLICA_COMPLETION_COUNT)),
                    )
                }
            }

        manager.jobs()
            .define(parameters.jobName.get())
            .withRegion(parameters.location.get())
            .withExistingResourceGroup(rg)
            .withEnvironmentId(envId)
            .withConfiguration(configuration)
            .withTemplate(template)
            .create()
    }

    private companion object {
        private const val DEFAULT_REPLICA_COMPLETION_COUNT = 1
    }

}
