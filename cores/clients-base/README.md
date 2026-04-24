# Clients Base

A Gradle plugin providing a shared, polymorphic registry of named service clients for use across build scripts and
custom Gradle plugins.

## Overview

`clients-base` centralizes the lifecycle of SDK clients (Artifactory, AWS, GCP, etc.) in a Gradle build:

- **Named registry**: Register clients by name with a typed configuration object; retrieve them from anywhere in the build
- **On-demand instantiation**: Clients are created only when first accessed and cached for the remainder of the build
- **Polymorphic**: Multiple client types can coexist in the same registry, each with its own configuration interface
- **Shared across the build**: Backed by a Gradle `BuildService`, so the same client instance is shared across all projects

This plugin is the foundation for other plugins in this suite (`artifactory-base`, AWS plugins, GCP plugins, etc.).
End users typically do not apply or interact with this plugin directly.

## Applying the Plugin

The plugin can be applied to a project, settings file, or init script.

### Project or settings

```kotlin
plugins {
    id("com.kelvsyc.gradle.clients-base")
}
```

### Init script

```kotlin
initscript {
    dependencies {
        classpath("com.kelvsyc.gradle:clients-base:<version>")
    }
}
apply<com.kelvsyc.gradle.plugins.ClientsBasePlugin>()
```

When applied to a settings file, the `serviceClients` extension is also created on all projects in that build.
When applied to an init script, the extension is created on all settings and projects. In both cases, clients
registered before a project evaluates are visible to that project without it needing to apply the plugin itself.

## Core Concepts

### Client info types

Each client type is represented by a pair of interfaces:

- **`ServiceClientInfo<C>`** (`Named`) — the public configuration interface. Holds the data needed to create a client
  of type `C`. This is what callers use to register and look up clients.
- **`ServiceClientInfoInternal<C>`** — extends `ServiceClientInfo<C>` and adds `createClient(): C`. This is the
  implementation type and is not part of the public API.

Plugin authors define both interfaces, then register the binding so the service knows which implementation to
instantiate:

```kotlin
// Public interface — callers configure this
interface MyClientInfo : ServiceClientInfo<MyClient> {
    val endpoint: Property<String>
}

// Internal implementation — creates the actual client
abstract class MyClientInfoInternal : MyClientInfo, ServiceClientInfoInternal<MyClient> {
    override fun createClient(): MyClient = MyClient(endpoint.get())
}
```

Register the binding once, typically in the plugin's `apply()` method:

```kotlin
serviceClients.service.get().registerBinding(MyClientInfo::class, MyClientInfoInternal::class)
```

### Registering a client

Once a binding is registered, callers can register named client instances:

```kotlin
serviceClients.service.get().registerIfAbsent<MyClientInfo>("myClient") {
    endpoint.set("https://example.com")
}
```

`registerIfAbsent` is idempotent: if a client with that name is already registered, the existing registration is
returned and the configuration block is not applied.

### Retrieving a client

Retrieve a client as a lazy `Provider` via the `serviceClients` extension:

```kotlin
val client: Provider<MyClient> = serviceClients.getClient<MyClient, MyClientInfo>("myClient")
```

The `Provider` has no value if no client with that name is registered, or if the registered client is of a
different type. Client instances are created on first access and cached.

The provider is typically wired into a task at configuration time and resolved at execution time:

```kotlin
tasks.register("doSomething") {
    val client: Provider<MyClient> = serviceClients.getClient<MyClient, MyClientInfo>("myClient")

    doLast {
        val c = client.get()
        // use c
    }
}
```

## Extension: `serviceClients`

The `serviceClients` extension is a `ClientsBaseExtension` backed by a `ClientsBaseService` build service.

### `ClientsBaseExtension`

| Member | Description |
|---|---|
| `service: Provider<ClientsBaseService>` | The underlying build service |
| `getClient<C, T>(name)` | Returns a `Provider<C>` for the registered client named `name`, where `T : ServiceClientInfo<C>` |

### `ClientsBaseService`

| Member | Description |
|---|---|
| `registrations` | The `ExtensiblePolymorphicDomainObjectContainer` holding all `ServiceClientInfo` registrations |
| `registerBinding(infoType, implType)` | Registers an implementation type for a client info interface |
| `registerIfAbsent(name, type) { }` | Registers a named client if not already registered |
| `registrationsWithType(type)` | Returns a filtered view of registrations for a given info type |
| `getClient(name, infoType, clientType)` | Returns the client instance (or `null`); creates and caches on first call |

## See Also

- [artifactory-base](../artifactory-base) — Client plugin built on top of `clients-base`
- [Gradle Build Services](https://docs.gradle.org/current/userguide/build_services.html) — Gradle documentation on shared build services
- [Gradle Providers](https://docs.gradle.org/current/userguide/lazy_configuration.html) — Gradle documentation on lazy evaluation