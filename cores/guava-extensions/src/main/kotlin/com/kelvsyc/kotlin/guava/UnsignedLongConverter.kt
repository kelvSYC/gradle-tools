package com.kelvsyc.kotlin.guava

import com.google.common.base.Converter
import com.google.common.primitives.UnsignedLong

/**
 * [Converter] implementation converting Guava [UnsignedLong] and Kotlin [ULong].
 */
object UnsignedLongConverter : Converter<UnsignedLong, ULong>() {
    override fun doBackward(b: ULong): UnsignedLong = b.asGuavaUnsignedLong

    override fun doForward(a: UnsignedLong): ULong = a.asULong
}
