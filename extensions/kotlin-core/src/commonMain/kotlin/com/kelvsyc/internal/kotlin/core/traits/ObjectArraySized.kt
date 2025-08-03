package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.ArraySized
import com.kelvsyc.kotlin.core.traits.Sized

// TODO should we offer byte alignment sized?
class ObjectArraySized<A, E>(override val arraySize: Int, override val elementSized: Sized<E>) : ArraySized<A, E>
