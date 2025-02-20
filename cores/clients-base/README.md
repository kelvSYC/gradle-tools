# kelvSYC Gradle Tools - Clients Base

This plugin provides a shared central storage for reusable clients, keyed by a client name.

# Usage

This plugin can be applied to projects (in a `build.gradle.kts` file), to settings files (in a `settings.gradle.kts`)
file, or to init scripts (in an `init.gradle.kts` file). When applying to project and settings, you can use the plugins
syntax common to both:

```kotlin
plugins {
    id("com.kelvsyc.gradle.clients-base")
}
```

For init scripts, you will need to do something more complicated:

```kotlin
initscript {
    dependencies {
        classpath("com.kelvsyc.gradle:clients-base")
    }
}
apply("com.kelvSYC.gradle.clients-base")
```

Generally, end users will not need to apply this plugin directly. Rather, end users will be applying plugins that are
built using this plugin as a foundation.

# Components
The Clients Base plugin will register a shared build service named `serviceClients`, which can be accessed through the
`serviceClients` extension.

If the plugin is applied to a settings file, the `serviceClients` extension will also be created in all projects therein.
However, the ability to configure the `serviceClients` extension may be limited unless the project also applies the
plugin. Similarly, if the plugin is applied to an init script, the `serviceClients` extension will be created in all
settings and projects, but the ability to configure it may be limited unless these settings and projects also apply the
plugin.

Configuration of the service can be done by accessing the service through the `service` property of the `serviceClients`
extension, and then using the service's registration function `registerIfAbsent()`.

```kotlin
val service = serviceClients.service.get()
service.registerIfAbsent<MyClientInfo>("myClient") {
    // ...
}
```

Registration of client info types can be done through the service's `registerBinding()` function:

```kotlin
service.registerBinding(MyClientInfo::class, MyClientInfoInternal::class)
```

Generally, end users will not need to use either of these two functions directly (or at all, in the case of
`registerBinding()`); instead, these are meant to be used as part of plugin implementations or the commissioning of
extension functions that simplify the process for the end user.

Retrieval of previously registered service clients can be done directly through the extension:

```kotlin
val client: Provider<MyClient> = serviceClients.getClient<MyClientInfo>("myClient")
```

Clients themselves are instantiated on demand whenever they are required, and are shared across the lifetime of the
build.

The use of `getClient()` allows for a common task design as follows:

## Client Types
In the Clients Base Plugin, registered clients are associated with a number of different types:

* The client info type, extending `ServiceClientInfo`
* The internal client info type, extending `ServiceClientInfoInternal`, which in turn extends `ServiceClientInfo`.
* The actual client type

The client info type is responsible for getting information about the creation of the service client, while the internal
client info type is responsible for instantiating the client type based on the supplied information. The registration
and retrieval of clients is done through the client info types, with each client info type being associated with a
single actual client type.
