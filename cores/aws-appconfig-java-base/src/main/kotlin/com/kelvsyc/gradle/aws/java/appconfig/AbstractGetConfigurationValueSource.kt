package com.kelvsyc.gradle.aws.java.appconfig

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * Abstract [ValueSource] template base for retrieving a deployed AppConfig configuration
 * and converting it to a custom value type.
 *
 * Extend this class and implement [convert] to transform the raw configuration bytes
 * into your desired type. Returns `null` when the configuration is unavailable or empty.
 *
 * Most users should use [GetConfigurationValueSource] for UTF-8 string retrieval.
 * Extend this class only if you need a different conversion (e.g., JSON, binary format).
 *
 * @param T the non-null value type returned by [obtain].
 */
abstract class AbstractGetConfigurationValueSource<T : Any> : ValueSource<T, AbstractGetConfigurationValueSource.Parameters> {
    /**
     * Common parameters for all [AbstractGetConfigurationValueSource] implementations.
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the AppConfig Data client. */
        @get:Internal
        val service: Property<AppConfigDataClientBuildService>

        /** Application name or ID. */
        val applicationIdentifier: Property<String>

        /** Environment name or ID. */
        val environmentIdentifier: Property<String>

        /** Configuration profile name or ID. */
        val configurationProfileIdentifier: Property<String>
    }

    /**
     * Converts raw configuration bytes to [T].
     *
     * @return the converted value, or `null` if conversion is not possible.
     */
    protected abstract fun convert(bytes: ByteArray): T?

    override fun obtain(): T? {
        val bytes = parameters.service.get().fetchConfiguration(
            parameters.applicationIdentifier.get(),
            parameters.environmentIdentifier.get(),
            parameters.configurationProfileIdentifier.get(),
        )
        if (bytes == null || bytes.isEmpty()) return null
        return convert(bytes)
    }
}
