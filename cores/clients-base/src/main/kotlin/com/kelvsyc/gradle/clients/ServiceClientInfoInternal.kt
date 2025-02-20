package com.kelvsyc.gradle.clients

/**
 * Base type for the internal type for [ServiceClientInfo].
 */
interface ServiceClientInfoInternal<T : Any> : ServiceClientInfo<T> {
    /**
     * Creates the service client. This function will be called exactly once during the lifetime of the build.
     */
    fun createClient(): T
}
