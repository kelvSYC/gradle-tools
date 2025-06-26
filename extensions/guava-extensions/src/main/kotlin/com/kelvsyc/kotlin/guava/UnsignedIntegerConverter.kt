package com.kelvsyc.kotlin.guava

import com.google.common.base.Converter
import com.google.common.primitives.UnsignedInteger

/**
 * [Converter] implementation converting Guava [UnsignedInteger] and Kotlin [UInt].
 */
object UnsignedIntegerConverter : Converter<UnsignedInteger, UInt>() {
    override fun doBackward(b: UInt): UnsignedInteger = b.asGuavaUnsignedInteger

    override fun doForward(a: UnsignedInteger): UInt = a.asUInt
}
