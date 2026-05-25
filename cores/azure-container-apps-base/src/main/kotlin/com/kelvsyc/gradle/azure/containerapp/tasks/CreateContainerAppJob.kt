package com.kelvsyc.gradle.azure.containerapp.tasks

import com.kelvsyc.gradle.azure.containerapp.ContainerAppsEnvironmentBuildService
import com.kelvsyc.gradle.azure.containerapp.JobTriggerType
import com.kelvsyc.gradle.azure.containerapp.actions.CreateContainerAppJobAction
import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Creates a new Azure Container App Job definition within the configured managed environment.
 *
 * Delegates to [CreateContainerAppJobAction] via [WorkerExecutor.noIsolation].
 */
@DisableCachingByDefault(because = "Creating a cloud resource is not cacheable")
abstract class CreateContainerAppJob @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /** The environment service. Resource group and environment name are read from its params. */
    @get:Internal
    abstract val environmentService: Property<ContainerAppsEnvironmentBuildService>

    /** Name of the new job. */
    @get:Input
    abstract val jobName: Property<String>

    /** Container image URI. */
    @get:Input
    abstract val imageUri: Property<String>

    /** Azure region. */
    @get:Input
    abstract val location: Property<String>

    /** How the job is triggered. */
    @get:Input
    abstract val triggerType: Property<JobTriggerType>

    /** Cron expression for [JobTriggerType.SCHEDULED] jobs. */
    @get:Input
    @get:Optional
    abstract val cronExpression: Property<String>

    /** Number of job replicas per execution. */
    @get:Input
    @get:Optional
    abstract val replicaCompletionCount: Property<Int>

    /** Optional environment variables. */
    @get:Input
    @get:Optional
    abstract val envVars: MapProperty<String, String>

    /** Submits [CreateContainerAppJobAction]. */
    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(CreateContainerAppJobAction::class.java) { params ->
            params.service.set(environmentService)
            params.jobName.set(jobName)
            params.imageUri.set(imageUri)
            params.location.set(location)
            params.triggerType.set(triggerType)
            params.cronExpression.set(cronExpression)
            params.replicaCompletionCount.set(replicaCompletionCount)
            params.envVars.set(envVars)
        }
    }
}
