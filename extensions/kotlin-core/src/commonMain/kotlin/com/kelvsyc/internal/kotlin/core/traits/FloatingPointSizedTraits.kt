package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.Sized

object BFloat16Sized : Sized {
    private const val SIZE_BITS = 16
    private const val SIZE_BYTES = SIZE_BITS / Byte.SIZE_BITS
    override val sizeBits: Int get() = SIZE_BITS
    override val sizeBytes: Int get() = SIZE_BYTES
}

object Binary16Sized : Sized {
    private const val SIZE_BITS = 16
    private const val SIZE_BYTES = SIZE_BITS / Byte.SIZE_BITS
    override val sizeBits: Int get() = SIZE_BITS
    override val sizeBytes: Int get() = SIZE_BYTES
}

object Binary32Sized : Sized {
    private const val SIZE_BITS = 32
    private const val SIZE_BYTES = SIZE_BITS / Byte.SIZE_BITS
    override val sizeBits: Int get() = SIZE_BITS
    override val sizeBytes: Int get() = SIZE_BYTES
}

object Binary64Sized : Sized {
    private const val SIZE_BITS = 64
    private const val SIZE_BYTES = SIZE_BITS / Byte.SIZE_BITS
    override val sizeBits: Int get() = SIZE_BITS
    override val sizeBytes: Int get() = SIZE_BYTES
}

object Binary128Sized : Sized {
    private const val SIZE_BITS = 128
    private const val SIZE_BYTES = SIZE_BITS / Byte.SIZE_BITS
    override val sizeBits: Int get() = SIZE_BITS
    override val sizeBytes: Int get() = SIZE_BYTES
}

object Binary256Sized : Sized {
    private const val SIZE_BITS = 256
    private const val SIZE_BYTES = SIZE_BITS / Byte.SIZE_BITS
    override val sizeBits: Int get() = SIZE_BITS
    override val sizeBytes: Int get() = SIZE_BYTES
}

object Decimal32Sized : Sized {
    private const val SIZE_BITS = 32
    private const val SIZE_BYTES = SIZE_BITS / Byte.SIZE_BITS
    override val sizeBits: Int get() = SIZE_BITS
    override val sizeBytes: Int get() = SIZE_BYTES
}

object Decimal64Sized : Sized {
    private const val SIZE_BITS = 64
    private const val SIZE_BYTES = SIZE_BITS / Byte.SIZE_BITS
    override val sizeBits: Int get() = SIZE_BITS
    override val sizeBytes: Int get() = SIZE_BYTES
}

object Decimal128Sized : Sized {
    private const val SIZE_BITS = 128
    private const val SIZE_BYTES = SIZE_BITS / Byte.SIZE_BITS
    override val sizeBits: Int get() = SIZE_BITS
    override val sizeBytes: Int get() = SIZE_BYTES
}
