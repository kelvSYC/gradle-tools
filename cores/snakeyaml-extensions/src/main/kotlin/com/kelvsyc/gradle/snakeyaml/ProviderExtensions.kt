package com.kelvsyc.gradle.snakeyaml

import com.kelvsyc.kotlin.snakeyaml.YamlMapping
import com.kelvsyc.kotlin.snakeyaml.YamlSequence
import com.kelvsyc.kotlin.snakeyaml.YamlValue
import com.kelvsyc.kotlin.snakeyaml.booleanAt
import com.kelvsyc.kotlin.snakeyaml.doubleAt
import com.kelvsyc.kotlin.snakeyaml.intAt
import com.kelvsyc.kotlin.snakeyaml.longAt
import com.kelvsyc.kotlin.snakeyaml.mappingAt
import com.kelvsyc.kotlin.snakeyaml.parseYaml
import com.kelvsyc.kotlin.snakeyaml.sequenceAt
import com.kelvsyc.kotlin.snakeyaml.stringAt
import org.gradle.api.provider.Provider

/**
 * Returns a [Provider] that lazily parses this string provider's value as a [YamlValue] tree.
 */
fun Provider<String>.parseYaml(): Provider<YamlValue> = map { it.parseYaml() }

/**
 * Returns a [Provider] providing the string value at the given path segments, or absent if the
 * path does not resolve to a scalar.
 */
fun Provider<YamlValue>.stringAt(vararg segments: String): Provider<String> =
    map { it.stringAt(*segments) }

/**
 * Returns a [Provider] providing the integer value at the given path segments, or absent if the
 * path does not resolve to a parseable integer.
 */
fun Provider<YamlValue>.intAt(vararg segments: String): Provider<Int> =
    map { it.intAt(*segments) }

/**
 * Returns a [Provider] providing the long value at the given path segments, or absent if the
 * path does not resolve to a parseable long.
 */
fun Provider<YamlValue>.longAt(vararg segments: String): Provider<Long> =
    map { it.longAt(*segments) }

/**
 * Returns a [Provider] providing the double value at the given path segments, or absent if the
 * path does not resolve to a parseable double.
 */
fun Provider<YamlValue>.doubleAt(vararg segments: String): Provider<Double> =
    map { it.doubleAt(*segments) }

/**
 * Returns a [Provider] providing the boolean value at the given path segments, or absent if the
 * path does not resolve to a recognized YAML boolean.
 */
fun Provider<YamlValue>.booleanAt(vararg segments: String): Provider<Boolean> =
    map { it.booleanAt(*segments) }

/**
 * Returns a [Provider] providing the [YamlMapping] at the given path segments, or absent if the
 * path does not resolve to a mapping.
 */
fun Provider<YamlValue>.mappingAt(vararg segments: String): Provider<YamlMapping> =
    map { it.mappingAt(*segments) }

/**
 * Returns a [Provider] providing the [YamlSequence] at the given path segments, or absent if the
 * path does not resolve to a sequence.
 */
fun Provider<YamlValue>.sequenceAt(vararg segments: String): Provider<YamlSequence> =
    map { it.sequenceAt(*segments) }
