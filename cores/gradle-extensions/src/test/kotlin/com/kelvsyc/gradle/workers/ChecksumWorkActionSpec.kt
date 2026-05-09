package com.kelvsyc.gradle.workers

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.file.shouldExist
import org.gradle.kotlin.dsl.newInstance
import org.gradle.testfixtures.ProjectBuilder

class ChecksumWorkActionSpec : FunSpec() {
    init {
        test("writes SHA-256 checksum to output file") {
            val project = ProjectBuilder.builder().build()
            val inputFile = tempfile()
            inputFile.writeText("hello")
            val outputDir = tempdir()
            val outputFile = outputDir.resolve("${inputFile.name}.sha256")

            val params = project.objects.newInstance<ChecksumWorkAction.Parameters>()
            params.inputFile.set(inputFile)
            params.algorithm.set("SHA-256")
            params.outputFile.set(outputFile)

            val action = object : ChecksumWorkAction() {
                override fun getParameters() = params
            }
            action.execute()

            outputFile.shouldExist()
            outputFile.readText() shouldBeEqual "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824\n"
        }

        test("writes MD5 checksum to output file") {
            val project = ProjectBuilder.builder().build()
            val inputFile = tempfile()
            inputFile.writeText("hello")
            val outputDir = tempdir()
            val outputFile = outputDir.resolve("${inputFile.name}.md5")

            val params = project.objects.newInstance<ChecksumWorkAction.Parameters>()
            params.inputFile.set(inputFile)
            params.algorithm.set("MD5")
            params.outputFile.set(outputFile)

            val action = object : ChecksumWorkAction() {
                override fun getParameters() = params
            }
            action.execute()

            outputFile.shouldExist()
            outputFile.readText() shouldBeEqual "5d41402abc4b2a76b9719d911017c592\n"
        }

        test("writes SHA-512 checksum to output file") {
            val project = ProjectBuilder.builder().build()
            val inputFile = tempfile()
            inputFile.writeText("hello")
            val outputDir = tempdir()
            val outputFile = outputDir.resolve("checksum.txt")

            val params = project.objects.newInstance<ChecksumWorkAction.Parameters>()
            params.inputFile.set(inputFile)
            params.algorithm.set("SHA-512")
            params.outputFile.set(outputFile)

            val action = object : ChecksumWorkAction() {
                override fun getParameters() = params
            }
            action.execute()

            outputFile.shouldExist()
        }
    }
}
