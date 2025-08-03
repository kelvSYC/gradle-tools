package com.kelvsyc.gradle.providers

import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.provider.ValueSourceSpec
import org.gradle.kotlin.dsl.of
import kotlin.reflect.KClass

// Internal workaround since Gradle libraries do not treat Action<in T> as T.() -> Unit
internal fun <T : Any, P : ValueSourceParameters> ProviderFactory.ofKt(
    valueSourceType: KClass<out ValueSource<T, P>>,
    configuration: ValueSourceSpec<P>.() -> Unit
) = of(valueSourceType, configuration)

/**
 * Returns a [Provider] that provides a constant value.
 *
 * The returned [Provider] is absent if `null` is supplied.
 */
fun <T : Any> ProviderFactory.ofNullable(value: T?): Provider<T> = provider { value }

/**
 * Returns a [Provider] whose value is always absent.
 */
val ProviderFactory.absent: Provider<Nothing>
    get() = provider { null }

/**
 * Returns a [Provider] providing a [Properties][java.util.Properties] object read from a file.
 *
 * @see [PropertiesFromFileValueSource]
 */
fun ProviderFactory.propertiesFile(file: RegularFile) = ofKt(PropertiesFromFileValueSource::class) {
    parameters.inputFile.set(file)
}

/**
 * Returns a [Provider] providing a [Properties][java.util.Properties] object read from a file.
 *
 * @see [PropertiesFromFileValueSource]
 */
fun ProviderFactory.propertiesFile(file: Provider<RegularFile>) = ofKt(PropertiesFromFileValueSource::class) {
    parameters.inputFile.set(file)
}
