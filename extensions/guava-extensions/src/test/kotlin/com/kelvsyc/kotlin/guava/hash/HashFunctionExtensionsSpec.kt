package com.kelvsyc.kotlin.guava.hash

import com.google.common.hash.HashFunction
import com.google.common.hash.Hasher
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence

class HashFunctionExtensionsSpec : FunSpec() {
    init {
        test("hash") {
            val hash = mockk<HashFunction>()
            val action = mockk<Hasher.() -> Unit>(relaxed = true)
            val hasher = mockk<Hasher>(relaxed = true)
            every { hash.newHasher() } returns hasher

            hash.hash(action)

            verifySequence {
                hash.newHasher()
                action.invoke(hasher)
                hasher.hash()
            }
        }

        test("hash with size") {
            val hash = mockk<HashFunction>()
            val size = 31
            val action = mockk<Hasher.() -> Unit>(relaxed = true)
            val hasher = mockk<Hasher>(relaxed = true)
            every { hash.newHasher(any()) } returns hasher

            hash.hash(size, action)

            verifySequence {
                hash.newHasher(size)
                action.invoke(hasher)
                hasher.hash()
            }
        }
    }
}
