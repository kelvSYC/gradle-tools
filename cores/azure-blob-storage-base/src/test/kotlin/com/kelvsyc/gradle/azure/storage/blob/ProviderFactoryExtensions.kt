package com.kelvsyc.gradle.azure.storage.blob

import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.provider.ValueSourceSpec
import org.gradle.kotlin.dsl.of
import kotlin.reflect.KClass

// Workaround: under the kotlin-gradle-library convention, Kotlin does not treat Gradle's
// Action<in T> as T.() -> Unit, so providers.of(...) { parameters.X } fails to resolve.
internal fun <T : Any, P : ValueSourceParameters> ProviderFactory.ofKt(
    valueSourceType: KClass<out ValueSource<T, P>>,
    configuration: ValueSourceSpec<P>.() -> Unit
) = of(valueSourceType, configuration)
