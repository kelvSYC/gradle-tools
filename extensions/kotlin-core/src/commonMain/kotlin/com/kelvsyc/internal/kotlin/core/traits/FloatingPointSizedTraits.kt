package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.Sized

class BFloat16Sized : Sized {
    companion object {
        internal const val SIZE_BITS = 16
    }
    override val sizeBits: Int get() = SIZE_BITS
}

class Binary16Sized : Sized {
    companion object {
        internal const val SIZE_BITS = 16
    }
    override val sizeBits: Int get() = SIZE_BITS
}

class Binary32Sized : Sized {
    companion object {
        internal const val SIZE_BITS = 32
    }
    override val sizeBits: Int get() = SIZE_BITS
}

class Binary64Sized : Sized {
    companion object {
        internal const val SIZE_BITS = 64
    }
    override val sizeBits: Int get() = SIZE_BITS
}

class Binary128Sized : Sized {
    companion object {
        internal const val SIZE_BITS = 128
    }
    override val sizeBits: Int get() = SIZE_BITS
}

class Binary256Sized : Sized {
    companion object {
        internal const val SIZE_BITS = 256
    }
    override val sizeBits: Int get() = SIZE_BITS
}

class Decimal32Sized : Sized {
    companion object {
        internal const val SIZE_BITS = 32
    }
    override val sizeBits: Int get() = SIZE_BITS
}

class Decimal64Sized : Sized {
    companion object {
        internal const val SIZE_BITS = 64
    }
    override val sizeBits: Int get() = SIZE_BITS
}

class Decimal128Sized : Sized {
    companion object {
        internal const val SIZE_BITS = 128
    }
    override val sizeBits: Int get() = SIZE_BITS
}
