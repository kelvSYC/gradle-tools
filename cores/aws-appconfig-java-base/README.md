# aws-appconfig-java-base

Gradle plugin providing AWS AppConfig integration via the AWS Java SDK v2.

## Overview

Offers two independent halves:

- **Pull** — retrieve the currently deployed configuration at build time, as a string `ValueSource` or a materialized file.
- **Push** — primitive `WorkAction` building blocks for lifecycle plugins to manage AppConfig resources and trigger deployments.

## BuildServices

| Service | Purpose |
|---|---|
| `AppConfigClientBuildService` | Wraps `AppConfigClient` for management operations |
| `AppConfigDataClientBuildService` | Wraps `AppConfigDataClient`; manages session tokens; exposes `fetchConfiguration()` |

Register both via `gradle.sharedServices.registerIfAbsent(...)`, configured with `AwsBuildServiceParams` (region + credentials).

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

Extend `AbstractGetConfigurationValueSource<T, P>` and override `convert(ByteArray): T?` for custom return types.

### DownloadConfigurationAction

Writes the configuration to a file via `WorkerExecutor.noIsolation()`:

```kotlin
workerExecutor.noIsolation().submit(DownloadConfigurationAction::class.java) {
    service.set(appConfigDataService)
    applicationIdentifier.set("my-app")
    environmentIdentifier.set("production")
    configurationProfileIdentifier.set("my-profile")
    outputFile.set(layout.buildDirectory.file("config/app.json"))
}
```

## Push

### Resource Setup WorkActions

One `WorkAction` each for Create/Update/Delete of: **Application**, **Environment**, **ConfigurationProfile**.

### Deployment Lifecycle

| WorkAction | Purpose |
|---|---|
| `CreateHostedConfigurationVersionAction` | Creates a new hosted configuration version; writes version number to `versionNumberFile` |
| `StartDeploymentAction` | Starts a deployment; writes deployment number to `deploymentNumberFile` |
| `WaitForDeploymentAction` | Polls until `COMPLETE` or `ROLLED_BACK`; throws `GradleException` on failure/timeout |
| `StopDeploymentAction` | Triggers rollback of an in-progress deployment |

A lifecycle plugin wires these together:
1. Submit `CreateHostedConfigurationVersionAction` → read `versionNumberFile`
2. Submit `StartDeploymentAction` with version → read `deploymentNumberFile`
3. Submit `WaitForDeploymentAction` with deployment number
