package com.kelvsyc.gradle.clients

import org.gradle.api.Named

/**
 * Information about the registration of a client.
 *
 * @param T The client type
 */
interface ServiceClientInfo<T : Any> : Named
