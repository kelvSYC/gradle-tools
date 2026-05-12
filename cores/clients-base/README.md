# Clients Base

A Gradle library providing base types for managing service client lifecycles as Gradle build services.

## Overview

`clients-base` provides `AbstractClientBuildService`, an abstract base class for build services that manage a single
service client instance. Each subclass defines its client configuration as serializable `BuildServiceParameters`,
ensuring full compatibility with Gradle's configuration cache.

- **Per-client build service**: Each client registration is its own `BuildService`, allowing Gradle to serialize and
  deserialize the service reference and its parameters natively
- **Lazy instantiation**: Clients are created on first access and cached for the lifetime of the build service
- **Configuration cache safe**: Client configuration lives in `BuildServiceParameters`, surviving configuration cache
  hits. Consuming `ValueSource` and `WorkAction` parameters hold a `Property<BuildService>`, which Gradle serializes
  natively
- **Type-safe**: Each service returns its concrete client type directly

This library is the foundation for other plugins in this suite (`artifactory-base`, AWS plugins, GCP plugins, etc.).

## Defining a Client Service

Subclass `AbstractClientBuildService` with the client type and a `BuildServiceParameters` interface containing the
serializable configuration needed to create the client:

```kotlin
abstract class MyClientBuildService :
    AbstractClientBuildService<MyClient, MyClientBuildService.Params>() {

    interface Params : BuildServiceParameters {
        val endpoint: Property<String>
    }

    override fun createClient(): MyClient = MyClient(parameters.endpoint.get())
}
```

### Registering a Client

Register the service using Gradle's `sharedServices`. Each named registration becomes an independent build service
instance with its own configuration:

```kotlin
val myClient = gradle.sharedServices.registerIfAbsent("my-client", MyClientBuildService::class) {
    parameters.endpoint.set("https://example.com")
}
```

Multiple clients of the same type can be registered with different names and configurations:

```kotlin
val prodClient = gradle.sharedServices.registerIfAbsent("my-client-prod", MyClientBuildService::class) {
    parameters.endpoint.set("https://prod.example.com")
}
val devClient = gradle.sharedServices.registerIfAbsent("my-client-dev", MyClientBuildService::class) {
    parameters.endpoint.set("https://dev.example.com")
}
```

### Using in ValueSources and WorkActions

Pass the service registration as a `Property<MyClientBuildService>` in the parameters interface. Gradle handles
serialization of the `BuildService` reference natively:

```kotlin
abstract class MyValueSource : ValueSource<String, MyValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        val service: Property<MyClientBuildService>
        val query: Property<String>
    }

    override fun obtain(): String? {
        val client = parameters.service.get().getClient()
        return client.fetch(parameters.query.get())
    }
}
```

### `AbstractClientBuildService`

| Member | Description |
|---|---|
| `getClient()` | Returns the managed client instance, creating it lazily on first access |
| `createClient()` | Abstract; subclasses implement to create the client from `parameters` |
| `close()` | Closes the client if it implements `AutoCloseable` and was created |

## Deprecated: `ClientsBaseService`

`ClientsBaseService` and its associated types (`ServiceClientInfo`, `ServiceClientInfoInternal`,
`ClientsBaseExtension`, `ClientsBasePlugin`) are deprecated. They manage a monolithic polymorphic registry of
clients in a single build service, which is incompatible with Gradle's configuration cache because the registry
state is populated imperatively during the configuration phase and lost on cache hits.

Existing consumers are being migrated to `AbstractClientBuildService` on a per-component basis.

## See Also

- [artifactory-base](../artifactory-base) — Client plugin built on top of `clients-base`
- [Gradle Build Services](https://docs.gradle.org/current/userguide/build_services.html) — Gradle documentation on shared build services
- [Gradle Providers](https://docs.gradle.org/current/userguide/lazy_configuration.html) — Gradle documentation on lazy evaluation
