# aws-appconfig-kotlin-base

Gradle plugin providing AWS AppConfig integration via the AWS Kotlin SDK.

## Overview

Offers two independent halves:

- **Pull** — retrieve the currently deployed configuration at build time, as a string `ValueSource` or a materialized file task.
- **Push** — `DefaultTask` building blocks for lifecycle plugins to manage AppConfig resources and trigger deployments.

## BuildServices

| Service | Purpose |
|---|---|
| `AppConfigClientBuildService` | Wraps `AppConfigClient` for management operations |
| `AppConfigDataClientBuildService` | Wraps `AppConfigDataClient`; manages session tokens; exposes `fetchConfiguration()` |

Register both via `gradle.sharedServices.registerIfAbsent(...)`, configured with `AwsBuildServiceParams` (region + credentials).

`AppConfigDataClientBuildService.fetchConfiguration()` is a `suspend` function. Calling it from a task `@TaskAction` requires `runBlocking { }`.

## Pull

### GetConfigurationValueSource

Returns the current deployed configuration as a `String`:

```kotlin
val config = providers.of(GetConfigurationValueSource::class) {
    parameters.service.set(appConfigDataService)
    parameters.applicationIdentifier.set("my-app")
    parameters.environmentIdentifier.set("production")
    parameters.configurationProfileIdentifier.set("my-profile")
}
```

Returns `null` when the configuration is unavailable or empty. Errors are logged as warnings.

Extend `AbstractGetConfigurationValueSource<T, P>` and override `convert(ByteArray): T?` for custom return types.

### DownloadConfigurationTask

Writes the current deployed configuration to a file:

```kotlin
tasks.register<DownloadConfigurationTask>("downloadConfig") {
    service.set(appConfigDataService)
    applicationIdentifier.set("my-app")
    environmentIdentifier.set("production")
    configurationProfileIdentifier.set("my-profile")
    outputFile.set(layout.buildDirectory.file("config/app.json"))
}
```

## Push

### Resource Setup Tasks

One task each for Create/Update/Delete of **Application**, **Environment**, and **ConfigurationProfile**:

| Task | Required properties | Optional properties |
|---|---|---|
| `CreateApplicationTask` | `applicationName` | `applicationDescription` |
| `UpdateApplicationTask` | `applicationId` | `applicationName`, `applicationDescription` |
| `DeleteApplicationTask` | `applicationId` | — |
| `CreateEnvironmentTask` | `applicationId`, `environmentName` | `environmentDescription` |
| `UpdateEnvironmentTask` | `applicationId`, `environmentId` | `environmentName`, `environmentDescription` |
| `DeleteEnvironmentTask` | `applicationId`, `environmentId` | — |
| `CreateConfigurationProfileTask` | `applicationId`, `profileName`, `locationUri`, `type` | `profileDescription` |
| `UpdateConfigurationProfileTask` | `applicationId`, `configurationProfileId` | `profileName`, `profileDescription` |
| `DeleteConfigurationProfileTask` | `applicationId`, `configurationProfileId` | — |

> **Note:** Property names differ from the Java base because `DefaultTask` inherits `getName(): String` and `getDescription(): String` from the Gradle `Task` interface. Using `name` or `description` as abstract property names on a `DefaultTask` subclass causes a JVM method signature conflict that prevents Gradle from generating the concrete subclass. The entity-scoped names (e.g. `applicationName`, `profileDescription`) avoid this and are also more explicit for the caller.

### Deployment Lifecycle Tasks

| Task | Purpose |
|---|---|
| `CreateHostedConfigurationVersionTask` | Creates a hosted configuration version; writes version number to `versionNumberFile` |
| `StartDeploymentTask` | Starts a deployment (see dual-mode behaviour below) |
| `WaitForDeploymentTask` | Waits via SDK waiter until deployment reaches a terminal state; throws `GradleException` on failure |
| `StopDeploymentTask` | Triggers rollback of an in-progress deployment |

#### `StartDeploymentTask` dual-mode

When `deploymentNumberFile` is **set**: the task starts the deployment, writes the deployment number to the file, and returns immediately. Use this when you need to perform other work between starting and waiting.

When `deploymentNumberFile` is **absent**: the task starts the deployment and immediately waits for it to reach a terminal state using the SDK's built-in `waitUntilDeploymentComplete` waiter.

A typical lifecycle plugin wires these tasks:

```kotlin
// Step 1 — create a hosted configuration version
tasks.register<CreateHostedConfigurationVersionTask>("createVersion") {
    service.set(appConfigService)
    applicationId.set("my-app")
    configurationProfileId.set("my-profile")
    content.set(file("config.json").readText())
    contentType.set("application/json")
    versionNumberFile.set(layout.buildDirectory.file("appconfig/version.txt"))
}

// Step 2 — deploy and wait inline
tasks.register<StartDeploymentTask>("deploy") {
    dependsOn("createVersion")
    service.set(appConfigService)
    applicationId.set("my-app")
    environmentId.set("production")
    configurationProfileId.set("my-profile")
    deploymentStrategyId.set("AppConfig.AllAtOnce")
    configurationVersion.set(
        layout.buildDirectory.file("appconfig/version.txt").map { it.asFile.readText().trim() }
    )
    // deploymentNumberFile not set → waits for completion inline
}
```

## Why no WorkActions

The AWS Kotlin SDK exposes all service calls as `suspend` functions. A `WorkAction` that wraps a single suspend call reduces to:

```kotlin
override fun execute() {
    runBlocking { singleSuspendCall() }
}
```

This adds ceremony with no benefit: no return values, no isolation beyond what coroutines already provide, and no concurrency advantage (Gradle's task graph handles cross-task concurrency; coroutines handle within-task concurrency). WorkActions were designed for blocking Java SDK calls to avoid tying up Gradle's worker thread pool — that problem doesn't exist with a coroutine-based SDK.

Accordingly, this component exposes `DefaultTask` subclasses instead. Plugin authors needing compound operations should compose via Gradle task dependencies (sequential) or call `service.get().getClient()` directly inside a `runBlocking { coroutineScope { } }` block (parallel).

Existing Kotlin SDK bases in this suite (`aws-secrets-manager-kotlin-base`, `aws-ssm-kotlin-base`, `aws-s3-kotlin-base`, and others) that currently use WorkActions are candidates for the same migration.
