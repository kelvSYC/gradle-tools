# Bitbucket Cloud Base

A Kotlin library providing managed Bitbucket Cloud REST API client integration using Retrofit, built on
`clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:bitbucket-cloud-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `BitbucketCloudClientBuildService` | `BitbucketCloudService` (Retrofit interface) |

```kotlin
val bb = gradle.sharedServices.registerIfAbsent("bb", BitbucketCloudClientBuildService::class) {
    parameters.username.set("user")
    parameters.password.set("app-password")
    // baseUrl defaults to https://api.bitbucket.org/2.0/
}
```

| Parameter | Type | Description |
|---|---|---|
| `baseUrl` | `Property<String>` | Base URL for the REST API; defaults to `https://api.bitbucket.org/2.0/` |
| `username` | `Property<String>` | Bitbucket Cloud username |
| `password` | `Property<String>` | Bitbucket Cloud app password |

## Value Sources

- `GetPullRequestValueSource` — fetches a single pull request
- `GetRepositoryValueSource` — fetches repository metadata
- `GetCommitStatusesValueSource` — fetches all build statuses for a commit (auto-paginates)

## WorkActions

- `CreatePullRequestCommentAction` — creates a Markdown comment on a pull request
- `PutCommitStatusAction` — creates or updates a build status on a commit

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [bitbucket-data-center-base](../bitbucket-data-center-base) — Bitbucket Data Center / Server variant
