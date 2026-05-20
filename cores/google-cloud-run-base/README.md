# Google Cloud Run Base

A Kotlin library providing managed Cloud Run client integration using the Google Cloud Java SDK,
built on `clients-base` and `google-cloud-extensions`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:google-cloud-run-base")
}
```

## Build Services

| Class | Client type |
|---|---|
| `CloudRunServicesClientBuildService` | `ServicesClient` (Cloud Run Services v2 API) |
| `CloudRunJobsClientBuildService` | `JobsClient` (Cloud Run Jobs v2 API) |
| `CloudRunExecutionsClientBuildService` | `ExecutionsClient` (Cloud Run Executions v2 API) |

```kotlin
val services = gradle.sharedServices.registerIfAbsent(
    "cloudRunServices",
    CloudRunServicesClientBuildService::class,
) {
    parameters {
        projectId.set("my-project")
        applicationDefault()
    }
}

val jobs = gradle.sharedServices.registerIfAbsent(
    "cloudRunJobs",
    CloudRunJobsClientBuildService::class,
) {
    parameters {
        projectId.set("my-project")
        applicationDefault()
    }
}

val executions = gradle.sharedServices.registerIfAbsent(
    "cloudRunExecutions",
    CloudRunExecutionsClientBuildService::class,
) {
    parameters {
        projectId.set("my-project")
        applicationDefault()
    }
}
```

Both `projectId` and the credentials call are optional. See
[google-cloud-extensions](../google-cloud-extensions) for the full set of credential configuration
functions.

## Value Sources

### `GetServiceValueSource`

Retrieves the HTTPS endpoint URL of a Cloud Run service. Returns `null` if the service does not
exist or has no deployed URI:

```kotlin
val serviceUri: Provider<String> = providers.of(GetServiceValueSource::class) {
    parameters {
        service.set(services)
        serviceName.set("projects/my-project/locations/us-central1/services/my-service")
    }
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<CloudRunServicesClientBuildService>` | Build service supplying the Services client |
| `serviceName` | `Property<String>` | Full resource name: `projects/{p}/locations/{l}/services/{s}` |

### `ListServicesValueSource`

Lists all Cloud Run services in a project and location, returning a `Map<String, String>` keyed by
short service name with the HTTPS endpoint URL as the value. Pagination is handled internally:

```kotlin
val allServices: Provider<Map<String, String>> = providers.of(ListServicesValueSource::class) {
    parameters {
        service.set(services)
        projectId.set("my-project")
        location.set("us-central1")
    }
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<CloudRunServicesClientBuildService>` | Build service supplying the Services client |
| `projectId` | `Property<String>` | GCP project ID |
| `location` | `Property<String>` | GCP region, e.g. `"us-central1"` |

### `GetJobValueSource`

Retrieves the latest execution name of a Cloud Run job. Returns `null` if the job does not exist
or has no executions:

```kotlin
val latestExecution: Provider<String> = providers.of(GetJobValueSource::class) {
    parameters {
        service.set(jobs)
        jobName.set("projects/my-project/locations/us-central1/jobs/my-job")
    }
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<CloudRunJobsClientBuildService>` | Build service supplying the Jobs client |
| `jobName` | `Property<String>` | Full resource name: `projects/{p}/locations/{l}/jobs/{j}` |

### `ListJobsValueSource`

Lists all Cloud Run jobs in a project and location, returning a list of short job names.
Pagination is handled internally:

```kotlin
val allJobs: Provider<List<String>> = providers.of(ListJobsValueSource::class) {
    parameters {
        service.set(jobs)
        projectId.set("my-project")
        location.set("us-central1")
    }
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<CloudRunJobsClientBuildService>` | Build service supplying the Jobs client |
| `projectId` | `Property<String>` | GCP project ID |
| `location` | `Property<String>` | GCP region, e.g. `"us-central1"` |

## WorkActions

### `UpsertServiceAction`

Creates or updates a Cloud Run Service (upsert semantics). The action fetches an existing service
by name; if not found, creates a new one. Blocks until the long-running operation completes:

```kotlin
workerExecutor.noIsolation().submit(UpsertServiceAction::class) {
    service.set(services)
    serviceName.set("projects/my-project/locations/us-central1/services/my-service")
    imageUri.set("gcr.io/my-project/image:v1.2.3")
    envVars.put("API_KEY", "secret-value")
    envVars.put("LOG_LEVEL", "DEBUG")
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<CloudRunServicesClientBuildService>` | Build service supplying the Services client |
| `serviceName` | `Property<String>` | Full resource name of the service |
| `imageUri` | `Property<String>` | Container image URI, e.g. `gcr.io/my-project/image:tag` |
| `envVars` | `MapProperty<String, String>` | Environment variables to set on the container |

### `DeleteServiceAction`

Deletes a Cloud Run Service. Blocks until the long-running operation completes:

```kotlin
workerExecutor.noIsolation().submit(DeleteServiceAction::class) {
    service.set(services)
    serviceName.set("projects/my-project/locations/us-central1/services/my-service")
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<CloudRunServicesClientBuildService>` | Build service supplying the Services client |
| `serviceName` | `Property<String>` | Full resource name of the service |

### `CreateJobAction`

Creates a new Cloud Run Job definition. Blocks until the long-running operation completes:

```kotlin
workerExecutor.noIsolation().submit(CreateJobAction::class) {
    service.set(jobs)
    jobName.set("projects/my-project/locations/us-central1/jobs/my-job")
    imageUri.set("gcr.io/my-project/image:v1.2.3")
    envVars.put("DATA_PATH", "/data")
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<CloudRunJobsClientBuildService>` | Build service supplying the Jobs client |
| `jobName` | `Property<String>` | Full resource name of the job to create |
| `imageUri` | `Property<String>` | Container image URI, e.g. `gcr.io/my-project/image:tag` |
| `envVars` | `MapProperty<String, String>` | Environment variables to set on the container |

### `UpdateJobAction`

Updates an existing Cloud Run Job definition's image and environment variables. Blocks until the
long-running operation completes:

```kotlin
workerExecutor.noIsolation().submit(UpdateJobAction::class) {
    service.set(jobs)
    jobName.set("projects/my-project/locations/us-central1/jobs/my-job")
    imageUri.set("gcr.io/my-project/image:v1.2.4")
    envVars.put("DATA_PATH", "/data")
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<CloudRunJobsClientBuildService>` | Build service supplying the Jobs client |
| `jobName` | `Property<String>` | Full resource name of the job to update |
| `imageUri` | `Property<String>` | Container image URI, e.g. `gcr.io/my-project/image:tag` |
| `envVars` | `MapProperty<String, String>` | Environment variables to set on the container |

### `DeleteJobAction`

Deletes a Cloud Run Job definition. Blocks until the long-running operation completes:

```kotlin
workerExecutor.noIsolation().submit(DeleteJobAction::class) {
    service.set(jobs)
    jobName.set("projects/my-project/locations/us-central1/jobs/my-job")
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<CloudRunJobsClientBuildService>` | Build service supplying the Jobs client |
| `jobName` | `Property<String>` | Full resource name of the job |

### `WaitForExecutionAction`

Polls a Cloud Run Job Execution until it reaches a terminal state (success or failure). Raises an
exception if the execution fails or the wait timeout is exceeded. Use with `RunJobTask` to wait
for a separately-submitted execution:

```kotlin
workerExecutor.noIsolation().submit(WaitForExecutionAction::class) {
    service.set(executions)
    executionName.set("projects/my-project/locations/us-central1/jobs/my-job/executions/abc123")
    pollIntervalMs.set(5000)      // default
    maxWaitTimeMs.set(1800000)    // 30 minutes (default)
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<CloudRunExecutionsClientBuildService>` | Build service supplying the Executions client |
| `executionName` | `Property<String>` | Full execution resource name |
| `pollIntervalMs` | `Property<Long>` | Poll interval in milliseconds (default: 5000) |
| `maxWaitTimeMs` | `Property<Long>` | Maximum wait time in milliseconds (default: 30 min) |

### Why There Is No `RunJobExecutionAction` WorkAction

Job submission is deliberately not implemented as a `WorkAction`. The Cloud Run API's job submission
method returns an execution name (a `String`) that must be threaded to `WaitForExecutionAction`.
WorkAction parameters are strictly input-only — there is no mechanism for a WorkAction to write
a return value back to the calling task.

Sharing the execution name via a companion-object or static variable would be an anti-pattern
that breaks under parallel task execution. Instead, job submission is performed inline in the
`@TaskAction` method of `AbstractRunJobTask`, which executes in the main task thread. The submission
API itself is fast (returns a long-running operation handle immediately); only the wait is delegated
to a `WorkAction` via `WaitForExecutionAction`. This is idiomatic for quick, value-returning API
calls in Gradle.

## Tasks

### `UpsertServiceTask` / `AbstractUpsertServiceTask`

A `DefaultTask` wrapper around `UpsertServiceAction` that declares `serviceName` and `imageUri` as
`@Input` properties, enabling Gradle dependency tracking:

```kotlin
tasks.register<UpsertServiceTask>("deployService") {
    service.set(services)
    serviceName.set("projects/my-project/locations/us-central1/services/my-service")
    imageUri.set("gcr.io/my-project/app:${project.version}")
    envVars.put("ENVIRONMENT", "production")
}
```

Use `AbstractUpsertServiceTask` only when you need to supply a custom `service` binding without
`@ServiceReference` tracking.

### `DeleteServiceTask` / `AbstractDeleteServiceTask`

A `DefaultTask` wrapper around `DeleteServiceAction`:

```kotlin
tasks.register<DeleteServiceTask>("deleteService") {
    service.set(services)
    serviceName.set("projects/my-project/locations/us-central1/services/my-service")
}
```

### `CreateJobTask` / `AbstractCreateJobTask`

A `DefaultTask` wrapper around `CreateJobAction`:

```kotlin
tasks.register<CreateJobTask>("createBatchJob") {
    service.set(jobs)
    jobName.set("projects/my-project/locations/us-central1/jobs/my-batch")
    imageUri.set("gcr.io/my-project/batch-processor:v1")
    envVars.put("BATCH_SIZE", "1000")
}
```

### `UpdateJobTask` / `AbstractUpdateJobTask`

A `DefaultTask` wrapper around `UpdateJobAction`:

```kotlin
tasks.register<UpdateJobTask>("updateBatchJob") {
    service.set(jobs)
    jobName.set("projects/my-project/locations/us-central1/jobs/my-batch")
    imageUri.set("gcr.io/my-project/batch-processor:v2")
    envVars.put("BATCH_SIZE", "2000")
}
```

### `DeleteJobTask` / `AbstractDeleteJobTask`

A `DefaultTask` wrapper around `DeleteJobAction`:

```kotlin
tasks.register<DeleteJobTask>("deleteBatchJob") {
    service.set(jobs)
    jobName.set("projects/my-project/locations/us-central1/jobs/my-batch")
}
```

### `RunJobTask` / `AbstractRunJobTask`

Submits a Cloud Run Job Execution and waits for it to complete. Job submission is performed
inline (not as a WorkAction); the execution name is optionally written to a file for use by
downstream tasks:

```kotlin
tasks.register<RunJobTask>("runBatchJob") {
    service.set(jobs)
    executionsService.set(executions)
    jobName.set("projects/my-project/locations/us-central1/jobs/my-batch")
    overrides.put("BATCH_SIZE", "5000")
    executionNameFile.set(layout.buildDirectory.file("run/execution-name.txt"))
}
```

`RunJobTask` declares both `service` and `executionsService` with `@ServiceReference`. Use
`AbstractRunJobTask` only when supplying custom service bindings without automatic tracking.

### `WaitForJobExecutionTask` / `AbstractWaitForJobExecutionTask`

Reads an execution name from a file (typically written by `RunJobTask`) and waits for that
execution to complete. Enables split submission-and-wait workflows for interleaving other
build work:

```kotlin
val runJob = tasks.register<RunJobTask>("runBatchJob") {
    service.set(jobs)
    executionsService.set(executions)
    jobName.set("projects/my-project/locations/us-central1/jobs/my-batch")
    executionNameFile.set(layout.buildDirectory.file("run/execution-name.txt"))
}

val waitForJob = tasks.register<WaitForJobExecutionTask>("waitForBatchJob") {
    service.set(executions)
    executionNameFile.set(runJob.flatMap { it.executionNameFile })
}
```

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [google-cloud-extensions](../google-cloud-extensions) — `GcpBuildServiceParams` and credential extensions
