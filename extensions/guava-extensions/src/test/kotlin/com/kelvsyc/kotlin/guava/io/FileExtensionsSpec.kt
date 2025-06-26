package com.kelvsyc.kotlin.guava.io

import com.google.common.io.Files
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.io.File
import java.nio.charset.Charset

class FileExtensionsSpec : FunSpec() {
    init {
        test("asByteSource") {
            mockkStatic(Files::class) {
                // FIXME workaround for https://github.com/mockk/mockk/issues/929
                every { Files.asByteSource(any()) } returns mockk()
                val file = mockk<File>()

                file.asByteSource()

                verify {
                    Files.asByteSource(file)
                }
            }
        }

        test("asByteSink") {
            mockkStatic(Files::class) {
                // FIXME workaround for https://github.com/mockk/mockk/issues/929
                every { Files.asByteSink(any()) } returns mockk()
                val file = mockk<File>()

                file.asByteSink()

                verify {
                    Files.asByteSink(file)
                }
            }
        }

        test("asCharSource") {
            mockkStatic(Files::class) {
                // FIXME workaround for https://github.com/mockk/mockk/issues/929
                every { Files.asCharSource(any(), any()) } returns mockk()
                val file = mockk<File>()

                file.asCharSource()

                verify {
                    Files.asCharSource(file, Charset.defaultCharset())
                }
            }
        }

        test("asCharSink") {
            mockkStatic(Files::class) {
                // FIXME workaround for https://github.com/mockk/mockk/issues/929
                every { Files.asCharSink(any(), any()) } returns mockk()
                val file = mockk<File>()

                file.asCharSink()

                verify {
                    Files.asCharSink(file, Charset.defaultCharset())
                }
            }
        }
    }
}
