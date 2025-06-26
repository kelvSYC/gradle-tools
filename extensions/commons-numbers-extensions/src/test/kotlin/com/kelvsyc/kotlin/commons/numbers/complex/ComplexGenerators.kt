package com.kelvsyc.kotlin.commons.numbers.complex

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.double
import org.apache.commons.numbers.complex.Complex

val arbitraryComplex = arbitrary {
    val re = Arb.double().bind()
    val im = Arb.double().bind()
    Complex.ofCartesian(re, im)
}
