package com.kelvsyc.gradle.moshi

import com.kelvsyc.kotlin.moshi.JsonValue
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
 * Returns a [Provider] providing a [JsonValue] tree parsed from a JSON file.
 *
 * @param file the JSON file to parse
 * @see JsonValueSource
 */
fun ProviderFactory.jsonFile(file: RegularFile): Provider<JsonValue> = ofKt(JsonValueSource::class) {
    parameters.inputFile.set(file)
}

/**
 * Returns a [Provider] providing a [JsonValue] tree parsed from a JSON file.
 *
 * @param file a provider for the JSON file to parse
 * @see JsonValueSource
 */
fun ProviderFactory.jsonFile(file: Provider<RegularFile>): Provider<JsonValue> = ofKt(JsonValueSource::class) {
    parameters.inputFile.set(file)
}

/**
 * Returns a [Provider] providing a string value extracted from a JSON file using a JsonPath
 * expression.
 *
 * @param file the JSON file to parse
 * @param jsonPath the JsonPath expression to evaluate
 * @see JsonPathValueSource
 */
fun ProviderFactory.jsonPath(file: RegularFile, jsonPath: String): Provider<String> =
    ofKt(JsonPathValueSource::class) {
        parameters.inputFile.set(file)
        parameters.jsonPath.set(jsonPath)
    }

/**
 * Returns a [Provider] providing a string value extracted from a JSON file using a JsonPath
 * expression.
 *
 * @param file a provider for the JSON file to parse
 * @param jsonPath the JsonPath expression to evaluate
 * @see JsonPathValueSource
 */
fun ProviderFactory.jsonPath(file: Provider<RegularFile>, jsonPath: String): Provider<String> =
    ofKt(JsonPathValueSource::class) {
        parameters.inputFile.set(file)
        parameters.jsonPath.set(jsonPath)
    }

/**
 * Returns a [Provider] providing a string value extracted from a JSON file using a JsonPath
 * expression.
 *
 * @param file the JSON file to parse
 * @param jsonPath a provider for the JsonPath expression to evaluate
 * @see JsonPathValueSource
 */
fun ProviderFactory.jsonPath(file: RegularFile, jsonPath: Provider<String>): Provider<String> =
    ofKt(JsonPathValueSource::class) {
        parameters.inputFile.set(file)
        parameters.jsonPath.set(jsonPath)
    }

/**
 * Returns a [Provider] providing a string value extracted from a JSON file using a JsonPath
 * expression.
 *
 * @param file a provider for the JSON file to parse
 * @param jsonPath a provider for the JsonPath expression to evaluate
 * @see JsonPathValueSource
 */
fun ProviderFactory.jsonPath(file: Provider<RegularFile>, jsonPath: Provider<String>): Provider<String> =
    ofKt(JsonPathValueSource::class) {
        parameters.inputFile.set(file)
        parameters.jsonPath.set(jsonPath)
    }
