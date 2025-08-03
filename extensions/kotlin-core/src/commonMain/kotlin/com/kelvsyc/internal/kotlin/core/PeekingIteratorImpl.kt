package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.PeekingIterator

open class PeekingIteratorImpl<T>(protected open val base: Iterator<T>) : PeekingIterator<T> {
    protected data class Holder<T>(val value: T)

    protected var peek: Holder<T>? = null

    override fun hasNext(): Boolean = peek != null || base.hasNext()

    override fun peek(): T {
        if (peek == null) {
            peek = Holder(base.next())
        }
        return peek!!.value
    }

    override fun next(): T {
        if (peek == null) {
            return base.next()
        } else {
            val result = peek!!.value
            peek = null
            return result
        }
    }
}
