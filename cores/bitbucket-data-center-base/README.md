# Bitbucket Data Center Base

A Kotlin library providing managed Bitbucket Data Center REST API client integration using Retrofit, built on
`clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:bitbucket-data-center-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `BitbucketServerClientBuildService` | `BitbucketServerService` (Retrofit interface) |

```kotlin
val bb = gradle.sharedServices.registerIfAbsent("bb", BitbucketServerClientBuildService::class) {
    parameters.baseUrl.set("https://bitbucket.example.com/")
    parameters.token.set(providers.environmentVariable("BITBUCKET_TOKEN"))
}
```

| Parameter | Type | Description |
|---|---|---|
| `baseUrl` | `Property<String>` | Base URL of the Bitbucket Data Center instance |
| `token` | `Property<String>` | Personal access token for authentication |

## Value Sources

- `GetPullRequestValueSource` — fetches a single pull request
- `GetRepositoryValueSource` — fetches repository metadata
- `GetBuildStatusesValueSource` — fetches all build statuses for a commit (auto-paginates)

## WorkActions

- `CreatePullRequestCommentAction` — creates a comment on a pull request
- `PostBuildStatusAction` — posts a build status to a commit

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [bitbucket-cloud-base](../bitbucket-cloud-base) — Bitbucket Cloud variant
