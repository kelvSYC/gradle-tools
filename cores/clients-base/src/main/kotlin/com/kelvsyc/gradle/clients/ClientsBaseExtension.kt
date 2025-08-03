package com.kelvsyc.gradle.clients

import org.gradle.api.provider.Provider
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * Gradle extension object providing access to a registered [ClientsBaseService] instance
 */
abstract class ClientsBaseExtension @Inject constructor(val service: Provider<ClientsBaseService>) {
    /**
     * Retrieves a [Provider] for the registered client with the specified name.
     *
     * The returned [Provider] will have no value if there is no registered client with that name, or if the registered
     * client with that name is of a different type.
     *
     * @param name              The registered client name
     * @param clientInfoType    The type of the registration
     * @param clientType        The type of the underlying client
     */
    fun <C : Any, T : ServiceClientInfo<C>> getClient(name: String, clientInfoType: Class<T>, clientType: Class<C>) =
        service.map { it.getClient(name, clientInfoType, clientType) }

    /**
     * Retrieves a [Provider] for the registered client with the specified name.
     *
     * The returned [Provider] will have no value if there is no registered client with that name, or if the registered
     * client with that name is of a different type.
     *
     * @param name              The registered client name
     * @param clientInfoType    The type of the registration
     * @param clientType        The type of the underlying client
     */
    fun <C : Any, T : ServiceClientInfo<C>> getClient(name: String, clientInfoType: KClass<T>, clientType: KClass<C>) =
        service.map { it.getClient(name, clientInfoType, clientType) }

    /**
     * Retrieves a [Provider] for the registered client with the specified name.
     *
     * The returned [Provider] will have no value if there is no registered client with that name, or if the registered
     * client with that name is of a different type.
     *
     * @param name  The registered client name
     * @param T     The type of the registration
     * @param C     The type of the underlying client
     */
    inline fun <reified C : Any, reified T : ServiceClientInfo<C>> getClient(name: String) =
        getClient(name, T::class, C::class)
}
