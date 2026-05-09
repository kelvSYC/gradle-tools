# Bitbucket Data Center Base

A Gradle plugin providing managed Bitbucket Data Center REST API client integration using Retrofit.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.bitbucket-data-center-base")
}
```

## Client Type

One client info type is registered:

| Client info type | Client type |
|---|---|
| `BitbucketServerClientInfo` | `BitbucketServerService` (Retrofit) |

`BitbucketServerClientInfo` extends `ServiceClientInfo` and provides `baseUrl` and `token` properties.
Register a client:

```kotlin
serviceClients.service.get().registerIfAbsent<BitbucketServerClientInfo>("bitbucket") {
    baseUrl.set("https://bitbucket.example.com/")
    token.set(providers.environmentVariable("BITBUCKET_TOKEN"))
}
```

> [!NOTE]
> The `baseUrl` must point to the server root (e.g. `https://bitbucket.example.com/`), not the API
> path. The plugin appends the correct API path prefixes internally.

## Value Sources

### `GetRepositoryValueSource`

Returns a `Repository` model with fields such as `slug`, `name`, `project`, and `forkable`:

```kotlin
val repo: Provider<Repository> = providers.of(GetRepositoryValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("bitbucket")
        projectKey.set("PROJ")
        repoSlug.set("my-repo")
    }
}
```

### `GetPullRequestValueSource`

Returns a `PullRequest` model with fields such as `title`, `state`, `fromRef`, `toRef`, and `author`:

```kotlin
val pr: Provider<PullRequest> = providers.of(GetPullRequestValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("bitbucket")
        projectKey.set("PROJ")
        repoSlug.set("my-repo")
        pullRequestId.set(7L)
    }
}
```

### `GetBuildStatusesValueSource`

Returns all build statuses for a commit as a `List<BuildStatus>`, automatically following pagination:

```kotlin
val statuses: Provider<List<BuildStatus>> = providers.of(GetBuildStatusesValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("bitbucket")
        commitId.set("abc123def456")
    }
}
```

> [!NOTE]
> The build status API (`/rest/build-status/1.0/`) is commit-scoped rather than repository-scoped,
> so only a `commitId` is required — no project key or repo slug.

## Work Actions

### `PostBuildStatusAction`

Posts a build status to a commit. The `state` must be one of `SUCCESSFUL`, `FAILED`, or `INPROGRESS`:

| Parameter | Required | Description |
|---|---|---|
| `service` | Yes | The shared `ClientsBaseService` |
| `clientName` | Yes | Registered client name |
| `commitId` | Yes | Full commit hash |
| `state` | Yes | Build state |
| `key` | Yes | Unique key for this build status |
| `url` | Yes | URL to the build results |
| `name` | No | Human-readable name |
| `description` | No | Status description |

### `CreatePullRequestCommentAction`

Posts a comment on a pull request:

| Parameter | Required | Description |
|---|---|---|
| `service` | Yes | The shared `ClientsBaseService` |
| `clientName` | Yes | Registered client name |
| `projectKey` | Yes | Project key |
| `repoSlug` | Yes | Repository slug |
| `pullRequestId` | Yes | Pull request ID |
| `text` | Yes | Comment text |

## Pagination

The Bitbucket Data Center API paginates list responses using `start`/`limit` query parameters with
`isLastPage`/`nextPageStart` in the response envelope. Two utilities are provided:

- **`paginatedSequence()`** — returns a lazy `Sequence<T>` that fetches pages on demand:

  ```kotlin
  val failedBuild = paginatedSequence { start ->
      service.getBuildStatuses("abc123", start = start)
  }.firstOrNull { it.state == "FAILED" }
  ```

- **`fetchAllPages()`** — eagerly collects all pages into a `List<T>`. Shorthand for
  `paginatedSequence(...).toList()`.

## Using the Client Directly

The `BitbucketServerService` Retrofit interface is exposed as the client type:

```kotlin
val client: BitbucketServerService =
    serviceClients.getClient<BitbucketServerService, BitbucketServerClientInfo>("bitbucket").get()
val prs = fetchAllPages { start ->
    client.listPullRequests("PROJ", "my-repo", start = start, state = "OPEN")
}
```

## Domain Model

Response types are in the `com.kelvsyc.gradle.bitbucket.server.model` package:

| Type | Description |
|---|---|
| `Repository` | Repository metadata |
| `Project` | Bitbucket project |
| `PullRequest` | Pull request metadata |
| `PullRequestRef` | Source or destination ref of a pull request |
| `PullRequestParticipant` | Author, reviewer, or observer on a pull request |
| `User` | User account |
| `BuildStatus` | Build status on a commit |
| `Comment` | Pull request comment |
| `PaginatedResponse<T>` | Paginated API response envelope |

## Differences from Bitbucket Cloud

| | Cloud (`bitbucket-cloud-base`) | Data Center (`bitbucket-data-center-base`) |
|---|---|---|
| Auth | App passwords (Basic) | Personal access tokens (Bearer) |
| Hierarchy | Workspaces → Repositories | Projects → Repositories |
| PR endpoint | `pullrequests` | `pull-requests` |
| PR refs | `source` / `destination` | `fromRef` / `toRef` |
| Build status | Repo-scoped | Commit-scoped (separate API) |
| Pagination | URL-based `next` links | `start`/`limit` + `nextPageStart` |
| Timestamps | ISO 8601 strings | Milliseconds since epoch |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [bitbucket-cloud-base](../bitbucket-cloud-base) — Bitbucket Cloud variant
