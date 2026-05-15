package com.kelvsyc.gradle.snakeyaml

import com.kelvsyc.kotlin.snakeyaml.YamlValue
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.provider.ValueSourceSpec
import org.gradle.kotlin.dsl.of
import kotlin.reflect.KClass

private fun <T : Any, P : ValueSourceParameters> ProviderFactory.ofKt(
    valueSourceType: KClass<out ValueSource<T, P>>,
    configuration: ValueSourceSpec<P>.() -> Unit
) = of(valueSourceType, configuration)

/**
 * Returns a [Provider] providing a [YamlValue] tree parsed from a YAML file.
 *
 * @param file the YAML file to parse
 * @see YamlValueSource
 */
fun ProviderFactory.yamlFile(file: RegularFile): Provider<YamlValue> = ofKt(YamlValueSource::class) {
    parameters.inputFile.set(file)
}

/**
 * Returns a [Provider] providing a [YamlValue] tree parsed from a YAML file.
 *
 * @param file a provider for the YAML file to parse
 * @see YamlValueSource
 */
fun ProviderFactory.yamlFile(file: Provider<RegularFile>): Provider<YamlValue> = ofKt(YamlValueSource::class) {
    parameters.inputFile.set(file)
}
