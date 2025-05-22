package com.kelvsyc.kotlin.commons.lang.reflect

import org.apache.commons.lang3.reflect.TypeUtils

/**
 * Builds a [WildcardType][java.lang.reflect.WildcardType] by applying a configuration action to a [WildcardTypeBuilder][TypeUtils.WildcardTypeBuilder].
 */
fun buildWildcardType(action: TypeUtils.WildcardTypeBuilder.() -> Unit) = TypeUtils.wildcardType().apply(action).build()
