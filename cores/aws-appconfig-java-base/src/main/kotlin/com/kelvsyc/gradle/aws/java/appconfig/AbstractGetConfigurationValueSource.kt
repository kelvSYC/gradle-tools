package com.kelvsyc.gradle.aws.java.appconfig

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * Abstract [ValueSource] base for retrieving a deployed AppConfig configuration and converting it
 * to a value of type [T].
 *
 * Subclasses implement [convert] to map the raw configuration bytes to the desired type.
 * Returns `null` when the configuration is unavailable or the byte array is empty.
 *
 * @param T the non-null value type returned by [obtain].
 * @param P the parameters type, which must extend [Parameters].
 */
abstract class AbstractGetConfigurationValueSource<T : Any, P : AbstractGetConfigurationValueSource.Parameters> :
    ValueSource<T, P> {
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
        ) ?: return null
        return if (bytes.isEmpty()) null else convert(bytes)
    }
}
