# Bitbucket Cloud Base

A Gradle plugin providing managed Bitbucket Cloud REST API client integration using Retrofit.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.bitbucket-cloud-base")
}
```

## Client Type

One client info type is registered:

| Client info type | Client type |
|---|---|
| `BitbucketCloudClientInfo` | `BitbucketCloudService` (Retrofit) |

`BitbucketCloudClientInfo` extends `ServiceClientInfo` and provides `baseUrl` and `credentials` properties.
Register a client:

```kotlin
serviceClients.service.get().registerIfAbsent<BitbucketCloudClientInfo>("bitbucket") {
    credentials.set(project.objects.newInstance<PasswordCredentials>().apply {
        username = "my-username"
        password = "my-app-password"
    })
}
```

The `baseUrl` defaults to `https://api.bitbucket.org/2.0/` and normally does not need to be overridden.

## Value Sources

### `GetRepositoryValueSource`

Returns a `Repository` model with fields such as `fullName`, `mainBranch`, `isPrivate`, and `owner`:

```kotlin
val repo: Provider<Repository> = providers.of(GetRepositoryValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("bitbucket")
        workspace.set("myworkspace")
        repoSlug.set("my-repo")
    }
}
val defaultBranch: Provider<String> = repo.map { it.mainBranch?.name ?: "main" }
```

### `GetPullRequestValueSource`

Returns a `PullRequest` model with fields such as `title`, `state`, `source`, `destination`, `author`,
and `reviewers`:

```kotlin
val pr: Provider<PullRequest> = providers.of(GetPullRequestValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("bitbucket")
        workspace.set("myworkspace")
        repoSlug.set("my-repo")
        pullRequestId.set(42L)
    }
}
```

### `GetCommitStatusesValueSource`

Returns all build statuses for a commit as a `List<CommitStatus>`, automatically following pagination:

```kotlin
val statuses: Provider<List<CommitStatus>> = providers.of(GetCommitStatusesValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("bitbucket")
        workspace.set("myworkspace")
        repoSlug.set("my-repo")
        commit.set("abc123def456")
    }
}
```

## Work Actions

### `PutCommitStatusAction`

Creates or updates a build status on a commit. The `state` must be one of `SUCCESSFUL`, `FAILED`,
`INPROGRESS`, or `STOPPED`:

| Parameter | Required | Description |
|---|---|---|
| `service` | Yes | The shared `ClientsBaseService` |
| `clientName` | Yes | Registered client name |
| `workspace` | Yes | Workspace slug or UUID |
| `repoSlug` | Yes | Repository slug |
| `commit` | Yes | Full commit hash |
| `key` | Yes | Unique key for this build status |
| `state` | Yes | Build state |
| `url` | Yes | URL to the build results |
| `name` | No | Human-readable name |
| `description` | No | Status description |

### `CreatePullRequestCommentAction`

Posts a Markdown comment on a pull request:

| Parameter | Required | Description |
|---|---|---|
| `service` | Yes | The shared `ClientsBaseService` |
| `clientName` | Yes | Registered client name |
| `workspace` | Yes | Workspace slug or UUID |
| `repoSlug` | Yes | Repository slug |
| `pullRequestId` | Yes | Pull request ID |
| `body` | Yes | Comment body (Markdown) |

## Pagination

The Bitbucket Cloud API paginates list responses. Two utilities are provided for transparent pagination:

- **`paginatedSequence()`** — returns a lazy `Sequence<T>` that fetches pages on demand. Use this when
  you only need a subset of results or want to avoid loading everything into memory:

  ```kotlin
  val failedBuild = paginatedSequence(
      firstPage = service.getCommitStatuses("ws", "repo", "abc123"),
      nextPage = service::getCommitStatusesPage,
  ).firstOrNull { it.state == "FAILED" }
  ```

- **`fetchAllPages()`** — eagerly collects all pages into a `List<T>`. Shorthand for
  `paginatedSequence(...).toList()`.

## Using the Client Directly

The `BitbucketCloudService` Retrofit interface is exposed as the client type, so you can use it for
API calls beyond what the built-in ValueSources and WorkActions cover:

```kotlin
val client: BitbucketCloudService = serviceClients.getClient<BitbucketCloudService, BitbucketCloudClientInfo>("bitbucket").get()
val prs = fetchAllPages(
    firstPage = client.listPullRequests("ws", "repo", mapOf("state" to "OPEN")),
    nextPage = client::listPullRequestsPage,
)
```

## Domain Model

Response types are in the `com.kelvsyc.gradle.bitbucket.cloud.model` package:

| Type | Description |
|---|---|
| `Repository` | Repository metadata |
| `PullRequest` | Pull request metadata |
| `PullRequestEndpoint` | Source or destination of a pull request |
| `CommitStatus` | Build status on a commit |
| `PullRequestComment` | Comment on a pull request |
| `Account` | User or team account |
| `Branch` | Branch reference |
| `Project` | Workspace project |
| `CommitSummary` | Lightweight commit reference |
| `RepositorySummary` | Lightweight repository reference |
| `CommentContent` | Comment body in multiple formats |
| `PaginatedResponse<T>` | Paginated API response envelope |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
