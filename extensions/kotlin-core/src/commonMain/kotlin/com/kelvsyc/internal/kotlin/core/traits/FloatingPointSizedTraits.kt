package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.Sized

class BFloat16Sized<T> : Sized<T> {
    companion object {
        internal const val SIZE_BITS = 16
    }
    override val sizeBits: Int get() = SIZE_BITS
}

class Binary16Sized<T> : Sized<T> {
    companion object {
        internal const val SIZE_BITS = 16
    }
    override val sizeBits: Int get() = SIZE_BITS
}

class Binary32Sized<T> : Sized<T> {
    companion object {
        internal const val SIZE_BITS = 32
    }
    override val sizeBits: Int get() = SIZE_BITS
}

class Binary64Sized<T> : Sized<T> {
    companion object {
        internal const val SIZE_BITS = 64
    }
    override val sizeBits: Int get() = SIZE_BITS
}

class Binary128Sized<T> : Sized<T> {
    companion object {
        internal const val SIZE_BITS = 128
    }
    override val sizeBits: Int get() = SIZE_BITS
}

class Binary256Sized<T> : Sized<T> {
    companion object {
        internal const val SIZE_BITS = 256
    }
    override val sizeBits: Int get() = SIZE_BITS
}

class Decimal32Sized<T> : Sized<T> {
    companion object {
        internal const val SIZE_BITS = 32
    }
    override val sizeBits: Int get() = SIZE_BITS
}

class Decimal64Sized<T> : Sized<T> {
    companion object {
        internal const val SIZE_BITS = 64
    }
    override val sizeBits: Int get() = SIZE_BITS
}
