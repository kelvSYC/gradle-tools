package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.ClassToInstanceMap
import com.google.common.collect.ImmutableClassToInstanceMap
import com.google.common.collect.MutableClassToInstanceMap
import com.google.common.reflect.ImmutableTypeToInstanceMap
import com.google.common.reflect.MutableTypeToInstanceMap
import com.google.common.reflect.TypeToInstanceMap
import com.kelvsyc.kotlin.guava.reflect.typeTokenOf

inline fun <B : Any, reified T : B> ClassToInstanceMap<B>.get() = getInstance(T::class.java)

inline fun <B : Any, reified T : B> ImmutableClassToInstanceMap.Builder<B>.put(value: T) = put(T::class.java, value)

inline fun <B : Any, reified T : B> MutableClassToInstanceMap<B>.put(value: T) = putInstance(T::class.java, value)

fun <B : Any> buildClassToInstanceMap(action: ImmutableClassToInstanceMap.Builder<B>.() -> Unit): ClassToInstanceMap<B> =
    ImmutableClassToInstanceMap.builder<B>().apply(action).build()

inline fun <B : Any, reified T : B> TypeToInstanceMap<B>.get() = getInstance(typeTokenOf<T>())

inline fun <B : Any, reified T : B> ImmutableTypeToInstanceMap.Builder<B>.put(value: T) = put(typeTokenOf(), value)

inline fun <B : Any, reified T : B> MutableTypeToInstanceMap<B>.put(value: T) = putInstance(typeTokenOf(), value)

fun <B : Any> buildTypeToInstanceMap(action: ImmutableTypeToInstanceMap.Builder<B>.() -> Unit): TypeToInstanceMap<B> =
    ImmutableTypeToInstanceMap.builder<B>().apply(action).build()
