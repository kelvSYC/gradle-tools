# azure-functions-base

Gradle build tooling for Azure Functions — deploy, invoke, and configure Azure Function Apps from
your Gradle build.

## Build Services

Services must be registered manually in the consuming build script:

```kotlin
val functions = gradle.sharedServices.registerIfAbsent(
    "functions",
    FunctionAppClientBuildService::class.java
) { spec ->
    spec.parameters.subscriptionId.set(providers.environmentVariable("AZURE_SUBSCRIPTION_ID"))
    spec.parameters.resourceGroup.set("my-resource-group")
    spec.parameters.defaultCredential()
}
```

For ARM management operations, `FunctionAppClientBuildService` is the entry point. To scope to a
single function, chain it with `FunctionClientBuildService`:

```kotlin
val functionService = gradle.sharedServices.registerIfAbsent(
    "myFunction",
    FunctionClientBuildService::class.java
) { spec ->
    spec.parameters.appService.set(functions)
    spec.parameters.appName.set("my-function-app")
    spec.parameters.functionName.set("my-function")
}
```

| Service | Client Type | Description |
|---|---|---|
| `FunctionAppClientBuildService` | `AppServiceManager` | ARM management plane; scoped to subscription + resource group |
| `FunctionClientBuildService` | `FunctionInfo` | Chained; scoped to a named app + function |

## ValueSources

| ValueSource | Service | Returns |
|---|---|---|
| `ListFunctionAppsValueSource` | `FunctionAppClientBuildService` | `Map<String, String>` — app name → default hostname |
| `GetFunctionAppValueSource` | `FunctionAppClientBuildService` + `appName` | `String?` — default hostname |
| `ListFunctionsValueSource` | `FunctionAppClientBuildService` + `appName` | `Map<String, String>` — function name → invoke URL template |
| `GetFunctionValueSource` | `FunctionClientBuildService` | `String?` — invoke URL template |

## WorkActions

| WorkAction | Description |
|---|---|
| `CallFunctionAction` | HTTP POST to an Azure Functions HTTP trigger. Configure auth via `anonymous()`, `functionKey(ref)`, or `azureAdToken(ref)`. |
| `ZipDeployFunctionAppAction` | Zip-deploy via the Kudu SCM API using ARM-retrieved git publishing credentials. |
| `RunFromPackageFunctionAppAction` | Sets `WEBSITE_RUN_FROM_PACKAGE`. Use `plainUrl(url)` or `sasUrl(ref)`. |
| `UpdateFunctionAppSettingsAction` | Updates app settings; supports `settings` (plain) and `sensitiveSettings` (via `CredentialReference`). |

## Tasks

| Task | Description |
|---|---|
| `DeployFunctionAppFromZip` | Wraps `ZipDeployFunctionAppAction`; `@InputFile zipFile` for up-to-date checking. |
| `DeployFunctionAppFromPackage` | Wraps `RunFromPackageFunctionAppAction` with plain URL; `@Input appName` and `@Input packageUrl`. |
| `AbstractDeployFunctionAppFromZip` | Abstract base; override `appService` without `@ServiceReference` tracking. |
| `AbstractDeployFunctionAppFromPackage` | Abstract base for custom up-to-date inputs (blob ETag, upstream artifact). |

## Deployment patterns

**Zip deploy** (simplest):
```kotlin
tasks.register<DeployFunctionAppFromZip>("deployFunction") {
    appService.set(functions)
    appName.set("my-function-app")
    zipFile.set(tasks.named<Zip>("packageFunction").flatMap { it.archiveFile })
}
```

**Run-from-package** (recommended for production):
```kotlin
tasks.register<DeployFunctionAppFromPackage>("deployFunction") {
    appService.set(functions)
    appName.set("my-function-app")
    packageUrl.set("https://mystorage.blob.core.windows.net/deploys/app.zip")
}
```

## Calling a function

```kotlin
workerExecutor.noIsolation().submit(CallFunctionAction::class.java) { params ->
    params.uri.set("https://myapp.azurewebsites.net/api/myfunction")
    params.functionKey(CredentialReference.EnvironmentVariable("FUNCTION_KEY"))
    params.payload.set("""{"key": "value"}""")
}
```

Auth is sent via the `x-functions-key` header — never as a `?code=` query parameter.

## Auth modes

| `FunctionAuthMode` | What is sent |
|---|---|
| `ANONYMOUS` | No credentials |
| `FUNCTION_KEY` | `x-functions-key: <key>` header |
| `AZURE_AD` | `Authorization: Bearer <token>` header |
