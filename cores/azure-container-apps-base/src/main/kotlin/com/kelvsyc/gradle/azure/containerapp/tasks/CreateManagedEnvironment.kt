package com.kelvsyc.gradle.azure.containerapp.tasks

import com.kelvsyc.gradle.azure.containerapp.ContainerAppsEnvironmentBuildService
import com.kelvsyc.gradle.azure.containerapp.actions.CreateManagedEnvironmentAction
import com.kelvsyc.gradle.clients.CredentialReference
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Creates an Azure Container Apps managed environment.
 *
 * Delegates to [CreateManagedEnvironmentAction] via [WorkerExecutor.noIsolation].
 */
@DisableCachingByDefault(because = "Creating a cloud resource is not cacheable")
abstract class CreateManagedEnvironment @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /** The environment service identifying the target subscription, resource group, and environment name. */
    @get:Internal
    abstract val environmentService: Property<ContainerAppsEnvironmentBuildService>

    /** Azure region where the environment will be created. */
    @get:Input
    abstract val location: Property<String>

    /** Log Analytics workspace customer ID (GUID). */
    @get:Input
    abstract val logAnalyticsWorkspaceId: Property<String>

    /** Reference to the Log Analytics workspace shared key. */
    @get:Internal
    abstract val logAnalyticsWorkspaceKey: Property<CredentialReference>

    /** Submits [CreateManagedEnvironmentAction]. */
    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(CreateManagedEnvironmentAction::class.java) { params ->
            params.service.set(environmentService)
            params.location.set(location)
            params.logAnalyticsWorkspaceId.set(logAnalyticsWorkspaceId)
            params.logAnalyticsWorkspaceKey.set(logAnalyticsWorkspaceKey)
        }
    }
}
