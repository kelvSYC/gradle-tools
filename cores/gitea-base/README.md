# Gitea Base

A Kotlin library providing managed Gitea/Forgejo REST API client integration using Retrofit, built on
`clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:gitea-base")
}
```

## Build Services

| Class | Client type | Auth |
|---|---|---|
| `GiteaBearerClientBuildService` | `GiteaService` (Retrofit interface) | Bearer token |
| `GiteaBasicClientBuildService` | `GiteaService` (Retrofit interface) | Basic auth |

### Bearer Token

```kotlin
val gitea = gradle.sharedServices.registerIfAbsent("gitea", GiteaBearerClientBuildService::class) {
    parameters.baseUrl.set("https://git.example.com/api/v1")
    parameters.bearerToken.set("ghp_xxxx...")
    // or use the extension helper:
    // parameters.bearerToken.set(providers.environmentVariable("GITEA_TOKEN"))
}
```

Convenience extension:

```kotlin
val gitea = gradle.sharedServices.registerIfAbsent("gitea", GiteaBearerClientBuildService::class) {
    parameters.baseUrl.set("https://git.example.com/api/v1")
    parameters.bearerToken(providers)  // reads GITEA_TOKEN env var
}
```

| Parameter | Type | Description |
|---|---|---|
| `baseUrl` | `Property<String>` | Base URL for the REST API (e.g., `https://git.example.com/api/v1`) |
| `bearerToken` | `Property<CredentialReference>` | Bearer token for authentication |

### Basic Auth

```kotlin
val gitea = gradle.sharedServices.registerIfAbsent("gitea", GiteaBasicClientBuildService::class) {
    parameters.baseUrl.set("https://git.example.com/api/v1")
    parameters.username.set("myuser")
    parameters.password.set("mypassword")
    // or use the extension helper:
    // parameters.basicAuth(providers)  // reads GITEA_PASSWORD env var
}
```

Convenience extension:

```kotlin
val gitea = gradle.sharedServices.registerIfAbsent("gitea", GiteaBasicClientBuildService::class) {
    parameters.baseUrl.set("https://git.example.com/api/v1")
    parameters.basicAuth(providers)  // reads GITEA_PASSWORD env var
}
```

| Parameter | Type | Description |
|---|---|---|
| `baseUrl` | `Property<String>` | Base URL for the REST API (e.g., `https://git.example.com/api/v1`) |
| `username` | `Property<String>` | Gitea/Forgejo username |
| `password` | `Property<CredentialReference>` | Gitea/Forgejo password or personal access token |

## Value Sources

- `GetRepositoryValueSource` — fetches repository metadata
- `GetPullRequestValueSource` — fetches a single pull request by index
- `GetPullRequestsValueSource` — fetches all pull requests (auto-paginates)
- `GetCommitStatusesValueSource` — fetches all commit statuses for a SHA (auto-paginates)

### Custom Paginated Value Sources

For implementing custom paginated queries, extend one of the abstract bases:

- `AbstractPaginatedValueSource<T>` — for single-result or early-termination queries
- `AbstractCollectedPaginatedValueSource<T>` — for collecting all paginated results

Both support early-termination via a predicate, avoiding unnecessary API calls.

## WorkActions

- `PostCommitStatusAction` — posts a build status to a commit
- `CreatePullRequestCommentAction` — creates a comment on a pull request

## Tasks

- `GetGiteaRepoArchive` — downloads a repository archive (format inferred from output file extension: `.tar.gz`, `.zip`, etc.)
- `DownloadGiteaReleaseArtifact` — downloads release assets by name

## Supported Platforms

- **Gitea** 1.0+
- **Forgejo** (compatible API subset)

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
