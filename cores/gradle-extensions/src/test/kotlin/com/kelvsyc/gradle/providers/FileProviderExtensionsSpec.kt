package com.kelvsyc.gradle.providers

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class FileProviderExtensionsSpec : FunSpec() {
    init {
        test("asAbsolutePath") {
            val project = ProjectBuilder.builder().build()
            val file = tempfile() // Shouldn't mock a File, so using a temp file
            val underlying = project.providers.provider { file }
            val provider = project.layout.file(underlying)

            val result = provider.asAbsolutePath
            val resultPath = result.get()

            resultPath shouldBeEqual file.absolutePath
        }

        test("asPath") {
            val project = ProjectBuilder.builder().build()
            val file = tempfile() // Shouldn't mock a File, so using a temp file
            val underlying = mockk<RegularFile>()
            every { underlying.asFile } returns file
            val provider = project.providers.provider { underlying }

            val result = provider.asPath
            val resultPath = result.get()

            resultPath shouldBeEqual file.toPath()
        }

        test("directory dir") {
            val project = ProjectBuilder.builder().build()
            val underlying = mockk<Directory>(relaxed = true)
            val directory = project.providers.provider { underlying }
            val path = "foo"

            val result = directory.dir(path)
            result.get()

            verify {
                underlying.dir(path)
            }
        }

        test("directory dir provider") {
            val project = ProjectBuilder.builder().build()
            val underlying = mockk<Directory>(relaxed = true)
            val directory = project.providers.provider { underlying }
            val path = project.providers.provider { "foo" }
            // Provider.flatMap() internals require us to stub out a result for underlying.dir()
            val resultUnderlying = tempfile() // Shouldn't mock a File, so using a temp file
            val resultDir = project.layout.dir(project.providers.provider { resultUnderlying })
            every { underlying.dir(any<Provider<String>>()) } returns resultDir

            val result = directory.dir(path)
            result.get()

            verify {
                underlying.dir(path)
            }
        }

        test("directory file") {
            val project = ProjectBuilder.builder().build()
            val underlying = mockk<Directory>(relaxed = true)
            val directory = project.providers.provider { underlying }
            val path = "foo"

            val result = directory.file(path)
            result.get()

            verify {
                underlying.file(path)
            }
        }

        test("directory file provider") {
            val project = ProjectBuilder.builder().build()
            val underlying = mockk<Directory>(relaxed = true)
            val directory = project.providers.provider { underlying }
            val path = project.providers.provider { "foo" }
            // Provider.flatMap() internals require us to stub out a result for underlying.file()
            val resultUnderlying = tempfile() // Shouldn't mock a File, so using a temp file
            val resultFile = project.layout.file(project.providers.provider { resultUnderlying })
            every { underlying.file(any<Provider<String>>()) } returns resultFile

            val result = directory.file(path)
            result.get()

            verify {
                underlying.file(path)
            }
        }
    }
}
