package com.kelvsyc.kotlin.guava.reflect

import com.google.common.reflect.TypeToken
import kotlin.reflect.KType
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalStdlibApi::class)
fun <T : Any> KType.toTypeToken(): TypeToken<T> = TypeToken.of(javaType) as TypeToken<T>

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T : Any> typeTokenOf() = TypeToken.of(typeOf<T>().javaType) as TypeToken<T>
