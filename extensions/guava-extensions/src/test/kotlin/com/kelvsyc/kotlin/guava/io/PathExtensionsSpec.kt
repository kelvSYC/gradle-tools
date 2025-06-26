package com.kelvsyc.kotlin.guava.io

import com.google.common.io.MoreFiles
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.nio.charset.Charset
import java.nio.file.Path

class PathExtensionsSpec : FunSpec() {
    init {
        test("asByteSource") {
            mockkStatic(MoreFiles::class) {
                // FIXME workaround for https://github.com/mockk/mockk/issues/929
                every { MoreFiles.asByteSource(any()) } returns mockk()
                val path = mockk<Path>()

                path.asByteSource()

                verify {
                    MoreFiles.asByteSource(path)
                }
            }
        }

        test("asByteSink") {
            mockkStatic(MoreFiles::class) {
                // FIXME workaround for https://github.com/mockk/mockk/issues/929
                every { MoreFiles.asByteSink(any()) } returns mockk()
                val path = mockk<Path>()

                path.asByteSink()

                verify {
                    MoreFiles.asByteSink(path)
                }
            }
        }

        test("asCharSource") {
            mockkStatic(MoreFiles::class) {
                // FIXME workaround for https://github.com/mockk/mockk/issues/929
                every { MoreFiles.asCharSource(any(), any()) } returns mockk()
                val path = mockk<Path>()

                path.asCharSource()

                verify {
                    MoreFiles.asCharSource(path, Charset.defaultCharset())
                }
            }
        }

        test("asCharSink") {
            mockkStatic(MoreFiles::class) {
                // FIXME workaround for https://github.com/mockk/mockk/issues/929
                every { MoreFiles.asCharSink(any(), any()) } returns mockk()
                val path = mockk<Path>()

                path.asCharSink()

                verify {
                    MoreFiles.asCharSink(path, Charset.defaultCharset())
                }
            }
        }

    }
}
