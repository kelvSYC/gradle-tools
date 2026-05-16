# Design: `nexus-base`

**Date:** 2026-05-16
**Status:** Approved

## Overview

`nexus-base` is a Gradle library component that provides managed Sonatype Nexus Repository Manager 3
client infrastructure for use in Gradle builds. It supports generic artifact upload and download
(including parallel batch operations) and a configuration-time ValueSource for artifacts whose
contents can fit in memory. Integration with `RepositoryHandler` or Gradle's publishing framework
is explicitly out of scope.

The component is a peer of `artifactory-base` in the dependency hierarchy: both depend on
`clients-base` and follow the same BuildService / ValueSource / WorkAction / Task pattern. The
implementation difference is that `nexus-base` uses Retrofit + OkHttp rather than a native SDK,
following the pattern established by `bitbucket-cloud-base`.

## Architecture

### Position in the dependency graph

```
clients-base  (AbstractClientBuildService, CredentialReference)
    └── nexus-base
```

No intermediate extensions layer is needed — Nexus is a single vendor with one HTTP surface.

### Module location

`cores/nexus-base`

### Convention plugin

`com.kelvsyc.internal.kotlin-gradle-library` — same as `artifactory-base` and all other service
base components.

### Dependencies

| Scope | Artifact | Rationale |
|---|---|---|
| `api` | `com.kelvsyc.gradle:clients-base` | `AbstractClientBuildService`, `CredentialReference` |
| `api` | `retrofit` | Kept as `api` to preserve a future Option B expansion path (see Future Work) |
| `api` | `moshi` | Kept as `api` for the same reason |
| `implementation` | `okhttp` | Transport detail; not part of the public API shape |
| `implementation` | `moshi-kotlin` | `KotlinJsonAdapterFactory` is a wiring detail |
| `implementation` | `retrofit-converter-moshi` | Converter registration is internal |
| `testImplementation` | `mockk` | WorkAction unit tests |

All dependencies are already present in the version catalog. No new BOM entries are required.

### Internal Retrofit interface

`NexusService` is the Retrofit service interface. It is `internal` — callers interact exclusively
through the typed BuildService, ValueSources, and tasks. Relevant endpoints:

- `@Streaming @GET("repository/{repository}/{path}") downloadAsset(...)` — streams artifact bytes
  from a raw repository by full path.
- `@Multipart @POST("service/rest/v1/components") uploadRawAsset(...)` — uploads a file to a raw
  repository using the Nexus REST v1 multipart format (`raw.directory`, `raw.asset1.filename`,
  `raw.asset1`).

## BuildService

### `NexusClientBuildService`

Extends `AbstractClientBuildService<NexusService, NexusClientBuildService.Params>`.

**Parameters:**

| Property | Type | Required | Notes |
|---|---|---|---|
| `baseUrl` | `Property<String>` | Yes | Always self-hosted; no default |
| `username` | `Property<String>` | No | Absent = anonymous access |
| `passwordRef` | `Property<CredentialReference>` | No | Ignored when `username` is absent |

`createClient()` builds an OkHttp client and wraps it in a Retrofit instance pointing at `baseUrl`.
If `username` is present, a basic-auth `Interceptor` is added to the OkHttp client; otherwise the
client is built without authentication (anonymous access).

**Extension functions on `Params`:**

```kotlin
/** Configures anonymous (unauthenticated) access. */
fun NexusClientBuildService.Params.anonymous()

/**
 * Configures HTTP Basic authentication.
 *
 * [password] defaults to [CredentialReference.EnvironmentVariable] resolving `NEXUS_PASSWORD`.
 * The credential value is resolved at build execution time and never enters the Gradle
 * configuration cache.
 */
fun NexusClientBuildService.Params.basicAuth(
    username: String,
    password: CredentialReference = CredentialReference.EnvironmentVariable("NEXUS_PASSWORD"),
)
```

Nexus user tokens (generated via the Nexus UI) are presented as a username/password pair and
consumed through `basicAuth()` without API difference.

## ValueSource

### `AbstractNexusArtifactValueSource<T : Any>`

A configuration-time ValueSource that downloads a single artifact by repository and path and
returns a caller-supplied transformation of the response bytes.

**Parameters:**

| Property | Type |
|---|---|
| `service` | `Property<NexusClientBuildService>` |
| `repository` | `Property<String>` |
| `path` | `Property<String>` |

`obtain()` calls `NexusService.downloadAsset()` with `@Streaming` (prevents OkHttp from buffering
the full response body in the JVM heap), then passes the response `InputStream` to the abstract
`doObtain(InputStream): T?` method. Callers subclass and implement `doObtain`.

**KDoc disclaimer (required on this class):**

> **Configuration cache and sensitive artifacts:** Gradle serializes the result of every
> `ValueSource.obtain()` call to the configuration cache in plaintext when the cache is written.
> Whatever `doObtain` returns — including any sensitive content the artifact may contain
> (credentials, private keys, tokens) — will be stored in `.gradle/configuration-cache/` and is
> readable by any process with access to the build directory. This applies regardless of how the
> resulting `Provider` is stored: wiring it into a task `@Input` property, a `@get:Internal`
> property, or a private `val` all cause `obtain()` to run at configuration time and the result to
> be cached.
>
> If the fetched artifact may contain sensitive data, call the `NexusClientBuildService` client
> directly inside a `WorkAction.execute()` body instead, where the result is never written to the
> cache. Non-sensitive artifacts (version manifests, metadata, changelogs) are safe to use at
> configuration time.

## WorkActions

Both WorkActions use `Property<NexusClientBuildService>` (no `@ServiceReference` — that annotation
applies to `DefaultTask` properties, not `WorkParameters`).

### `DownloadArtifactAction`

**Parameters:** `service`, `repository`, `path`, `outputFile: RegularFileProperty`

`execute()` calls `downloadAsset()` with `@Streaming` and pipes the response `InputStream` to
`outputFile` via `InputStream.copyTo(OutputStream)`, keeping heap usage constant regardless of
artifact size.

### `UploadArtifactAction`

**Parameters:** `service`, `repository`, `path`, `inputFile: RegularFileProperty`

`execute()` splits `path` on the last `/`:
- Everything before the last `/` becomes `raw.directory` in the multipart body.
- The tail becomes `raw.asset1.filename`.
- A path with no `/` (root-level file) sends an empty string for `raw.directory`.
- `raw.directory` does **not** carry a leading `/` — the split produces `com/example/1.0` not
  `/com/example/1.0`. This matches the Nexus REST v1 examples in the official documentation.

This split is an internal implementation detail of the Nexus raw-format REST API and is not part
of the caller-facing API. The caller-facing coordinate is always `repository` + `path`.

## Tasks

Two abstract/concrete pairs, following the `artifactory-base` pattern exactly.

### Download pair

**`AbstractBatchDownloadFromNexus`** — `DefaultTask` with:
- Inner class `Artifact(name: String) : Named` with `repository: Property<String>`,
  `path: Property<String>`, `outputFile: RegularFileProperty`.
- `fun registerArtifact(name: String, action: Action<Artifact>)`
- `@get:OutputFiles val outputFiles: FileCollection`
- `@TaskAction` submits one `DownloadArtifactAction` per registered artifact via
  `WorkerExecutor.noIsolation()`.

**`BatchDownloadFromNexus`** — concrete subclass that adds:
- `@get:ServiceReference val service: Property<NexusClientBuildService>`

### Upload pair

**`AbstractBatchUploadToNexus`** — same structure with `inputFile: RegularFileProperty` and
`@get:InputFiles @get:PathSensitive(PathSensitivity.NONE) val inputFiles`.

**`BatchUploadToNexus`** — concrete subclass with `@get:ServiceReference`.

## Testing

### `MockNexusClientBuildService`

Located in `src/test/kotlin`. Overrides `createClient()` to return `mockk<NexusService>()`. Since
`NexusService` is `internal`, it is visible from test sources within the same module.

### `DownloadArtifactActionTest`

Instantiates `DownloadArtifactAction` as an anonymous subclass with `getParameters()` overridden.
Stubs `mockNexusService.downloadAsset(any(), any())` to return a mock `ResponseBody` backed by a
fixed byte array. Calls `execute()` and asserts `outputFile` contains the expected bytes.

### `UploadArtifactActionTest`

Same structure. Stubs `uploadRawAsset(...)` to return a successful empty response. After
`execute()`, verifies:
- `uploadRawAsset` was called with the expected `repository`.
- The `raw.directory` and `raw.asset1.filename` multipart fields correctly reflect the split of the
  input `path`.
- Edge case: a `path` containing no `/` results in an empty `raw.directory` and a `raw.asset1.filename`
  equal to the full path.

No integration tests for v1 — there is no hosted Nexus instance available in CI. Integration tests
can be added when a test environment is provisioned.

## README

The component README must include:

1. A usage example showing `BuildServiceRegistry.registerIfAbsent` for `NexusClientBuildService`
   with both `basicAuth()` and `anonymous()` configurations.
2. A usage example for `BatchDownloadFromNexus` and `BatchUploadToNexus` task registration.
3. A usage example showing how to implement `AbstractNexusArtifactValueSource`.
4. A configuration cache safety callout:

> **Configuration cache and sensitive artifacts:** `AbstractNexusArtifactValueSource` serializes
> its result to the Gradle configuration cache in plaintext. Do not use it to fetch artifacts whose
> contents are sensitive (credentials, private keys, tokens). Fetch sensitive content inside a
> `WorkAction` at task execution time instead — the result is resolved after the cache has been
> read and is never written to it.

## Future Work

### Option C — Search-based asset discovery

Add a `SearchAssetsValueSource` wrapping `GET /service/rest/v1/search/assets`. Callers could
discover artifact paths at configuration time by querying repository, name, version, and other
criteria rather than supplying the full path statically. Requires a Moshi data model for search
result pages and pagination handling.

### Option B — Arbitrary REST ValueSource

Add an `AbstractNexusRequestValueSource<T>` for arbitrary Nexus REST GET calls (Moshi-parsed
response). Deferred unless there is a concrete need to expand the underlying Retrofit client
surface. If pursued, `NexusService` would need to be promoted from `internal` to public.

### GitLab Generic Packages and Azure Artifacts Universal Packages

Companion components following this same design (Option A only), to be designed separately.
