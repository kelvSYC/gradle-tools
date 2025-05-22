package com.kelvsyc.kotlin.commons.lang.reflect

import org.apache.commons.lang3.reflect.TypeLiteral

inline fun <reified T : Any> typeLiteralOf() = object : TypeLiteral<T>() {}
