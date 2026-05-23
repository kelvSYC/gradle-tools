package com.kelvsyc.gradle.aws.kotlin.appconfig

import org.gradle.api.provider.ValueSource

/**
 * [ValueSource] implementation that retrieves the current deployed AppConfig configuration as a
 * UTF-8 decoded [String].
 *
 * Returns `null` when the configuration is unavailable or empty. Errors are logged as warnings
 * and result in a `null` return rather than a thrown exception.
 */
abstract class GetConfigurationValueSource :
    AbstractGetConfigurationValueSource<String, GetConfigurationValueSource.Parameters>() {

    /**
     * Parameters for [GetConfigurationValueSource].
     */
    interface Parameters : AbstractGetConfigurationValueSource.Parameters

    override fun convert(bytes: ByteArray): String = bytes.toString(Charsets.UTF_8)
}
