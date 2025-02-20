package com.kelvsyc.gradle.clients

import org.gradle.api.Action
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.internal.PolymorphicDomainObjectContainerInternal
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.kotlin.dsl.containerWithType
import org.gradle.kotlin.dsl.registerBinding
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

/**
 * Gradle build service providing a centralized repository of service clients.
 */
@Suppress("detekt:TooManyFunctions")
abstract class ClientsBaseService : BuildService<BuildServiceParameters.None>, AutoCloseable {
    abstract val registrations: ExtensiblePolymorphicDomainObjectContainer<ServiceClientInfo<*>>

    private val clients = ConcurrentHashMap<String, Any>()

    @Suppress("BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER")
    fun <C : Any, T : ServiceClientInfo<C>, U> registerBinding(clientInfoType: Class<T>, implementationType: Class<U>)
        where U : T, U : ServiceClientInfoInternal<C> {
        // FIXME creatableTypes is an internal API, but there is no public API to introspect bindings
        val clients = registrations as PolymorphicDomainObjectContainerInternal<*>
        if (!clients.createableTypes.contains(clientInfoType)) {
            registrations.registerBinding(clientInfoType, implementationType)
        }
    }

    @Suppress("BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER")
    fun <C : Any, T : ServiceClientInfo<C>, U> registerBinding(clientInfoType: KClass<T>, implementationType: KClass<U>)
        where U : T, U : ServiceClientInfoInternal<C> {
        // FIXME creatableTypes is an internal API, but there is no public API to introspect bindings
        val clients = registrations as PolymorphicDomainObjectContainerInternal<*>
        if (!clients.createableTypes.contains(clientInfoType.java)) {
            registrations.registerBinding(clientInfoType, implementationType)
        }
    }

    /**
     * Returns a container of all registrations for a given client type.
     *
     * @param clientInfoType    The type of the client registration
     */
    fun <T : ServiceClientInfo<*>> registrationsWithType(clientInfoType: Class<T>) =
        registrations.containerWithType(clientInfoType)

    /**
     * Returns a container of all registrations for a given client type.
     *
     * @param clientInfoType    The type of the client registration
     */
    fun <T : ServiceClientInfo<*>> registrationsWithType(clientInfoType: KClass<T>) =
        registrations.containerWithType(clientInfoType)

    fun <T : ServiceClientInfo<*>> registerIfAbsent(
        name: String,
        clientInfoType: Class<T>,
        configureAction: Action<in T>
    ) = registrationsWithType(clientInfoType).let {
        if (it.names.contains(name)) {
            it.named(name)
        } else {
            it.register(name, configureAction)
        }
    }

    fun <T : ServiceClientInfo<*>> registerIfAbsent(
        name: String,
        clientInfoType: KClass<T>,
        configureAction: T.() -> Unit
    ) = registrationsWithType(clientInfoType).let {
        if (it.names.contains(name)) {
            it.named(name)
        } else {
            it.register(name, configureAction)
        }
    }

    inline fun <reified T : ServiceClientInfo<*>> registerIfAbsent(
        name: String,
        noinline configurationAction: T.() -> Unit
    ) = registerIfAbsent(name, T::class, configurationAction)

    /**
     * Retrieves the underlying client with the registered name. Client instances are created on demand, and are
     * cached for later use once created.
     *
     * Returns `null` if there are no registered clients with that name, or if the registered client with that name is
     * not o fthe specified tgype.
     *
     * @param name  The registered name of the client
     * @param clientInfoType    The type of the client registration
     * @param clientType    The underlying client type
     */
    fun <C : Any, T: ServiceClientInfo<C>> getClient(name: String, clientInfoType: Class<T>, clientType: Class<C>): C? {
        val infos = registrationsWithType(clientInfoType)
        return clientType.cast(
            name.takeIf { infos.names.contains(it) }?.let {
                @Suppress("UNCHECKED_CAST")
                val info = infos.named(it).get() as ServiceClientInfoInternal<C>
                clients.getOrPut(it) {
                    info.createClient()
                }
            }
        )
    }

    /**
     * Retrieves the underlying client with the registered name. Client instances are created on demand, and are
     * cached for later use once created.
     *
     * Returns `null` if there are no registered clients with that name, or if the registered client with that name is
     * not o fthe specified tgype.
     *
     * @param name  The registered name of the client
     * @param clientInfoType    The type of the client registration
     * @param clientType    The underlying client type
     */
    fun <C : Any, T : ServiceClientInfo<C>> getClient(
        name: String,
        clientInfoType: KClass<T>,
        clientType: KClass<C>
    ): C? {
        val infos = registrationsWithType(clientInfoType)
        return clientType.safeCast(
            name.takeIf { infos.names.contains(it) }?.let {
                @Suppress("UNCHECKED_CAST")
                val info = infos.named(it).get() as ServiceClientInfoInternal<C>
                clients.getOrPut(it) {
                    info.createClient()
                }
            }
        )
    }

    /**
     * Retrieves the underlying client with the registered name. Client instances are created on demand, and are
     * cached for later use once created.
     *
     * Returns `null` if there are no registered clients with that name, or if the registered client with that name is
     * not o fthe specified tgype.
     *
     * @param name  The registered name of the client
     * @param T The type of the client registration
     * @param C The underlying client type
     */
    inline fun <reified C : Any, reified T : ServiceClientInfo<C>> getClient(name: String) =
        getClient(name, T::class, C::class)

    override fun close() {
        clients.values.filterIsInstance<AutoCloseable>().forEach(AutoCloseable::close)
    }
}
