package com.kelvsyc.gradle.pkl

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
    configuration: ValueSourceSpec<P>.() -> Unit,
) = of(valueSourceType, configuration)

/**
 * Returns a [Provider] providing a string value extracted from a Pkl file using a
 * dot-notation path expression.
 *
 * @param file the Pkl file to evaluate
 * @param pklPath the dot-notation path expression
 * @see PklPathValueSource
 */
fun ProviderFactory.pklPath(file: RegularFile, pklPath: String): Provider<String> =
    ofKt(PklPathValueSource::class) {
        parameters.inputFile.set(file)
        parameters.pklPath.set(pklPath)
    }

/**
 * Returns a [Provider] providing a string value extracted from a Pkl file using a
 * dot-notation path expression.
 *
 * @param file a provider for the Pkl file to evaluate
 * @param pklPath the dot-notation path expression
 * @see PklPathValueSource
 */
fun ProviderFactory.pklPath(file: Provider<RegularFile>, pklPath: String): Provider<String> =
    ofKt(PklPathValueSource::class) {
        parameters.inputFile.set(file)
        parameters.pklPath.set(pklPath)
    }

/**
 * Returns a [Provider] providing a string value extracted from a Pkl file using a
 * dot-notation path expression.
 *
 * @param file the Pkl file to evaluate
 * @param pklPath a provider for the dot-notation path expression
 * @see PklPathValueSource
 */
fun ProviderFactory.pklPath(file: RegularFile, pklPath: Provider<String>): Provider<String> =
    ofKt(PklPathValueSource::class) {
        parameters.inputFile.set(file)
        parameters.pklPath.set(pklPath)
    }

/**
 * Returns a [Provider] providing a string value extracted from a Pkl file using a
 * dot-notation path expression.
 *
 * @param file a provider for the Pkl file to evaluate
 * @param pklPath a provider for the dot-notation path expression
 * @see PklPathValueSource
 */
fun ProviderFactory.pklPath(file: Provider<RegularFile>, pklPath: Provider<String>): Provider<String> =
    ofKt(PklPathValueSource::class) {
        parameters.inputFile.set(file)
        parameters.pklPath.set(pklPath)
    }
