package com.kelvsyc.gradle.aws.java.appconfig

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation that retrieves the current deployed AppConfig configuration
 * as a UTF-8 decoded [String].
 *
 * Returns `null` when the configuration is unavailable or empty. Errors are logged as warnings
 * and result in a `null` return rather than a thrown exception.
 */
abstract class GetConfigurationValueSource : ValueSource<String, GetConfigurationValueSource.Parameters> {
    /**
     * Parameters for [GetConfigurationValueSource].
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

    override fun obtain(): String? {
        val bytes = parameters.service.get().fetchConfiguration(
            parameters.applicationIdentifier.get(),
            parameters.environmentIdentifier.get(),
            parameters.configurationProfileIdentifier.get(),
        )
        if (bytes == null || bytes.isEmpty()) return null
        return bytes.toString(Charsets.UTF_8)
    }
}
