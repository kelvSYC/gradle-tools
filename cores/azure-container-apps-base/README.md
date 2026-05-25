# azure-container-apps-base

Gradle build tooling for Azure Container Apps — create, update, and delete managed environments, container apps, revisions, and jobs from your Gradle build.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:azure-container-apps-base")
}
```

## Build Services

Services must be registered manually in the consuming build script. The root service `ContainerAppsEnvironmentBuildService` is the credential anchor; scoped services chain from it.

### Environment service

```kotlin
val environment = gradle.sharedServices.registerIfAbsent(
    "containerAppsEnvironment",
    ContainerAppsEnvironmentBuildService::class.java
) { spec ->
    spec.parameters.subscriptionId.set("00000000-0000-0000-0000-000000000000")
    spec.parameters.resourceGroupName.set("my-resource-group")
    spec.parameters.environmentName.set("my-environment")
    spec.parameters.defaultCredential()
    // or:
    // spec.parameters.managedIdentity()
    // spec.parameters.clientSecret(tenantId, clientId, secret)
}
```

| Parameter | Type | Description |
|---|---|---|
| `subscriptionId` | `Property<String>` | Azure subscription ID |
| `resourceGroupName` | `Property<String>` | Resource group containing the managed environment |
| `environmentName` | `Property<String>` | Name of the Container Apps managed environment |
| `credentialSource` | `Property<AzureCredentialSource>` | Token credential to use. Set via extension functions. |

### Scoped services

To scope to a single container app:

```kotlin
val app = gradle.sharedServices.registerIfAbsent(
    "myApp",
    ContainerAppBuildService::class.java
) { spec ->
    spec.parameters.environmentService.set(environment)
    spec.parameters.containerAppName.set("my-app")
}
```

To scope to a single container app job:

```kotlin
val job = gradle.sharedServices.registerIfAbsent(
    "myJob",
    ContainerAppJobBuildService::class.java
) { spec ->
    spec.parameters.environmentService.set(environment)
    spec.parameters.jobName.set("my-job")
}
```

| Service | Client Type | Description |
|---|---|---|
| `ContainerAppsEnvironmentBuildService` | `ContainerAppsApiManager` | ARM management plane; scoped to subscription + resource group + environment |
| `ContainerAppBuildService` | `ContainerApp` | Chained; scoped to a named app within the environment |
| `ContainerAppJobBuildService` | `ContainerAppJob` | Chained; scoped to a named job within the environment |

## Pull (ValueSources)

### Environment ValueSources

**`GetManagedEnvironmentValueSource`** — Returns the default domain of a managed environment (e.g. `my-env.eastus.azurecontainerapps.io`), or `null` if not found or the domain is not available.

```kotlin
val provisioningState: Provider<String> = providers.of(GetManagedEnvironmentValueSource::class) {
    parameters {
        service.set(environment)
    }
}
```

**`ListContainerAppsValueSource`** — Returns a `List<String>` of container app names in the managed environment.

```kotlin
val apps: Provider<List<String>> = providers.of(ListContainerAppsValueSource::class) {
    parameters {
        service.set(environment)
    }
}
```

**`ListContainerAppJobsValueSource`** — Returns a `List<String>` of job names in the managed environment.

```kotlin
val jobs: Provider<List<String>> = providers.of(ListContainerAppJobsValueSource::class) {
    parameters {
        service.set(environment)
    }
}
```

### App ValueSources

**`GetContainerAppValueSource`** — Returns the ingress FQDN for a container app, or `null` if ingress is not configured or the app does not exist.

```kotlin
val fqdn: Provider<String> = providers.of(GetContainerAppValueSource::class) {
    parameters {
        service.set(app)
    }
}
```

**`ListRevisionsValueSource`** — Returns a `List<String>` of revision names for a container app.

```kotlin
val revisions: Provider<List<String>> = providers.of(ListRevisionsValueSource::class) {
    parameters {
        service.set(app)
    }
}
```

**`GetRevisionValueSource`** — Returns the running state of a revision, or `null` if not found.

```kotlin
val state: Provider<String> = providers.of(GetRevisionValueSource::class) {
    parameters {
        service.set(app)
        revisionName.set("my-app--abc123")
    }
}
```

### Job ValueSources

**`GetContainerAppJobValueSource`** — Returns the provisioning state of a job, or `null` if not found or still provisioning.

```kotlin
val provisioningState: Provider<String> = providers.of(GetContainerAppJobValueSource::class) {
    parameters {
        service.set(job)
    }
}
```

**`ListJobExecutionsValueSource`** — Returns a `List<String>` of job execution names for a job.

```kotlin
val executions: Provider<List<String>> = providers.of(ListJobExecutionsValueSource::class) {
    parameters {
        service.set(job)
    }
}
```

**`GetJobExecutionValueSource`** — Returns the status of a job execution, or `null` if not found.

```kotlin
val status: Provider<String> = providers.of(GetJobExecutionValueSource::class) {
    parameters {
        service.set(job)
        executionName.set("my-job-abc123")
    }
}
```

## Push (WorkActions)

### Environment WorkActions

**`CreateManagedEnvironmentAction`** — Creates a Container Apps managed environment.

```kotlin
workerExecutor.noIsolation().submit(CreateManagedEnvironmentAction::class) {
    service.set(environment)
    location.set("eastus")
}
```

**`DeleteManagedEnvironmentAction`** — Deletes a managed environment.

```kotlin
workerExecutor.noIsolation().submit(DeleteManagedEnvironmentAction::class) {
    service.set(environment)
}
```

### Container App WorkActions

**`CreateContainerAppAction`** — Creates a container app in the managed environment.

```kotlin
workerExecutor.noIsolation().submit(CreateContainerAppAction::class) {
    service.set(environment)
    containerAppName.set("my-app")
    imageUri.set("myregistry.azurecr.io/myapp:1.0.0")
    envVars.set(mapOf("KEY" to "value"))  // optional
}
```

**`UpdateContainerAppAction`** — Updates the image and environment variables of a container app.

```kotlin
workerExecutor.noIsolation().submit(UpdateContainerAppAction::class) {
    service.set(app)
    imageUri.set("myregistry.azurecr.io/myapp:2.0.0")
    envVars.set(mapOf("KEY" to "new-value"))  // optional
}
```

**`DeleteContainerAppAction`** — Deletes a container app.

```kotlin
workerExecutor.noIsolation().submit(DeleteContainerAppAction::class) {
    service.set(app)
}
```

### Revision WorkActions

**`ActivateRevisionAction`** — Activates a revision, making it serve traffic.

```kotlin
workerExecutor.noIsolation().submit(ActivateRevisionAction::class) {
    service.set(app)
    revisionName.set("my-app--abc123")
}
```

**`DeactivateRevisionAction`** — Deactivates a revision, stopping it from serving traffic.

```kotlin
workerExecutor.noIsolation().submit(DeactivateRevisionAction::class) {
    service.set(app)
    revisionName.set("my-app--abc123")
}
```

### Job WorkActions

**`CreateContainerAppJobAction`** — Creates a Container App job in the managed environment.

```kotlin
workerExecutor.noIsolation().submit(CreateContainerAppJobAction::class) {
    service.set(environment)
    jobName.set("my-job")
    imageUri.set("myregistry.azurecr.io/myjob:1.0.0")
    envVars.set(mapOf("KEY" to "value"))  // optional
    triggerType.set(JobTriggerType.SCHEDULED)            // or JobTriggerType.EVENT or JobTriggerType.MANUAL
}
```

**`UpdateContainerAppJobAction`** — Updates the image and environment variables of a job.

```kotlin
workerExecutor.noIsolation().submit(UpdateContainerAppJobAction::class) {
    service.set(job)
    imageUri.set("myregistry.azurecr.io/myjob:2.0.0")
    envVars.set(mapOf("KEY" to "new-value"))  // optional
}
```

**`DeleteContainerAppJobAction`** — Deletes a Container App job.

```kotlin
workerExecutor.noIsolation().submit(DeleteContainerAppJobAction::class) {
    service.set(job)
}
```

**`StartJobExecutionAction`** — Starts a job execution; writes the execution name to a file.

```kotlin
workerExecutor.noIsolation().submit(StartJobExecutionAction::class) {
    service.set(job)
    outputFile.set(layout.buildDirectory.file("executions/execution-name.txt"))
}
```

**`StopJobExecutionAction`** — Stops a running job execution.

```kotlin
workerExecutor.noIsolation().submit(StopJobExecutionAction::class) {
    service.set(job)
    executionName.set("my-job-abc123")
}
```

## Tasks

Each WorkAction has a corresponding `DefaultTask` subclass for convenience:

| Task | Description |
|---|---|
| `CreateManagedEnvironmentTask` | Wraps `CreateManagedEnvironmentAction` |
| `DeleteManagedEnvironmentTask` | Wraps `DeleteManagedEnvironmentAction` |
| `CreateContainerAppTask` | Wraps `CreateContainerAppAction` |
| `UpdateContainerAppTask` | Wraps `UpdateContainerAppAction` |
| `DeleteContainerAppTask` | Wraps `DeleteContainerAppAction` |
| `ActivateRevisionTask` | Wraps `ActivateRevisionAction` |
| `DeactivateRevisionTask` | Wraps `DeactivateRevisionAction` |
| `CreateContainerAppJobTask` | Wraps `CreateContainerAppJobAction` |
| `UpdateContainerAppJobTask` | Wraps `UpdateContainerAppJobAction` |
| `DeleteContainerAppJobTask` | Wraps `DeleteContainerAppJobAction` |
| `StartJobExecutionTask` | Wraps `StartJobExecutionAction` |
| `StopJobExecutionTask` | Wraps `StopJobExecutionAction` |

## Credential Configuration

Configure credentials using extension functions on `ContainerAppsEnvironmentBuildService.Params`:

**`defaultCredential()`** — Uses the Azure SDK's default credential chain: environment variables → managed identity → Azure CLI → IDE integration.

```kotlin
parameters.defaultCredential()
```

**`managedIdentity(clientId?)`** — Uses a managed identity. Provide `clientId` for user-assigned identities; omit for system-assigned.

```kotlin
parameters.managedIdentity()                    // system-assigned
parameters.managedIdentity(clientId = "...")    // user-assigned
```

**`clientSecret(tenantId, clientId, secret)`** — Uses service principal authentication with a client secret.

```kotlin
parameters.clientSecret(
    tenantId = "00000000-0000-0000-0000-000000000000",
    clientId = "11111111-1111-1111-1111-111111111111",
    secret = CredentialReference.ofEnvVar("AZURE_CLIENT_SECRET")
)
```

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [azure-extensions](../azure-extensions) — Azure credential helpers and `AzureBuildServiceParams`
