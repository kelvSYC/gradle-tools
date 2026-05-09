package com.kelvsyc.gradle.workers

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.file.shouldExist
import org.gradle.kotlin.dsl.newInstance
import org.gradle.testfixtures.ProjectBuilder
import java.util.zip.ZipFile

class ZipActionSpec : FunSpec() {
    init {
        test("creates a ZIP with relative entry paths from base directory") {
            val project = ProjectBuilder.builder().build()
            val baseDir = tempdir()
            baseDir.resolve("a.txt").writeText("alpha")
            baseDir.resolve("sub").mkdir()
            baseDir.resolve("sub/b.txt").writeText("bravo")

            val outputDir = tempdir()
            val outputFile = outputDir.resolve("out.zip")

            val params = project.objects.newInstance<ZipAction.Parameters>()
            params.baseDirectory.set(baseDir)
            params.sourceFiles.from(project.fileTree(baseDir))
            params.outputFile.set(outputFile)

            val action = object : ZipAction() {
                override fun getParameters() = params
            }
            action.execute()

            outputFile.shouldExist()
            ZipFile(outputFile).use { zip ->
                val entries = zip.entries().asSequence().map { it.name }.toList()
                entries shouldContainExactlyInAnyOrder listOf("a.txt", "sub/b.txt")
                zip.getInputStream(zip.getEntry("a.txt")).bufferedReader().readText() shouldBeEqual "alpha"
                zip.getInputStream(zip.getEntry("sub/b.txt")).bufferedReader().readText() shouldBeEqual "bravo"
            }
        }

        test("files outside base directory use file name only") {
            val project = ProjectBuilder.builder().build()
            val baseDir = tempdir()
            val outsideFile = tempdir().resolve("outside.txt")
            outsideFile.writeText("external")

            val outputDir = tempdir()
            val outputFile = outputDir.resolve("out.zip")

            val params = project.objects.newInstance<ZipAction.Parameters>()
            params.baseDirectory.set(baseDir)
            params.sourceFiles.from(outsideFile)
            params.outputFile.set(outputFile)

            val action = object : ZipAction() {
                override fun getParameters() = params
            }
            action.execute()

            outputFile.shouldExist()
            ZipFile(outputFile).use { zip ->
                val entries = zip.entries().asSequence().map { it.name }.toList()
                entries shouldContainExactlyInAnyOrder listOf("outside.txt")
                zip.getInputStream(zip.getEntry("outside.txt")).bufferedReader().readText() shouldBeEqual "external"
            }
        }

        test("respects compression level") {
            val project = ProjectBuilder.builder().build()
            val baseDir = tempdir()
            baseDir.resolve("data.txt").writeText("a".repeat(1000))

            val outputDir = tempdir()
            val compressed = outputDir.resolve("compressed.zip")
            val stored = outputDir.resolve("stored.zip")

            val compressedParams = project.objects.newInstance<ZipAction.Parameters>()
            compressedParams.baseDirectory.set(baseDir)
            compressedParams.sourceFiles.from(baseDir.resolve("data.txt"))
            compressedParams.outputFile.set(compressed)
            compressedParams.compressionLevel.set(9)

            val compressedAction = object : ZipAction() {
                override fun getParameters() = compressedParams
            }
            compressedAction.execute()

            val storedParams = project.objects.newInstance<ZipAction.Parameters>()
            storedParams.baseDirectory.set(baseDir)
            storedParams.sourceFiles.from(baseDir.resolve("data.txt"))
            storedParams.outputFile.set(stored)
            storedParams.compressionLevel.set(0)

            val storedAction = object : ZipAction() {
                override fun getParameters() = storedParams
            }
            storedAction.execute()

            compressed.shouldExist()
            stored.shouldExist()
            assert(compressed.length() < stored.length()) {
                "Compressed (${compressed.length()}) should be smaller than stored (${stored.length()})"
            }
        }

        test("skips directories in source files") {
            val project = ProjectBuilder.builder().build()
            val baseDir = tempdir()
            baseDir.resolve("file.txt").writeText("content")
            baseDir.resolve("subdir").mkdir()

            val outputDir = tempdir()
            val outputFile = outputDir.resolve("out.zip")

            val params = project.objects.newInstance<ZipAction.Parameters>()
            params.baseDirectory.set(baseDir)
            params.sourceFiles.from(baseDir.resolve("file.txt"), baseDir.resolve("subdir"))
            params.outputFile.set(outputFile)

            val action = object : ZipAction() {
                override fun getParameters() = params
            }
            action.execute()

            outputFile.shouldExist()
            ZipFile(outputFile).use { zip ->
                val entries = zip.entries().asSequence().map { it.name }.toList()
                entries shouldContainExactlyInAnyOrder listOf("file.txt")
            }
        }
    }
}
