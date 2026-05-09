package com.kelvsyc.gradle.providers

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class ChecksumValueSourceSpec : FunSpec() {
    init {
        test("SHA-256 checksum of known content") {
            val project = ProjectBuilder.builder().build()
            val file = tempfile()
            file.writeText("hello")
            val regularFile = project.layout.file(project.providers.ofNullable(file))

            val result = project.providers.checksum(regularFile, "SHA-256")

            result.get() shouldBeEqual "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"
        }

        test("MD5 checksum of known content") {
            val project = ProjectBuilder.builder().build()
            val file = tempfile()
            file.writeText("hello")
            val regularFile = project.layout.file(project.providers.ofNullable(file))

            val result = project.providers.checksum(regularFile, "MD5")

            result.get() shouldBeEqual "5d41402abc4b2a76b9719d911017c592"
        }

        test("empty file produces valid checksum") {
            val project = ProjectBuilder.builder().build()
            val file = tempfile()
            val regularFile = project.layout.file(project.providers.ofNullable(file))

            val result = project.providers.checksum(regularFile, "SHA-256")

            result.get().shouldNotBeNull()
            result.get() shouldBeEqual "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        }

        test("file not found returns absent") {
            val project = ProjectBuilder.builder().build()
            val baseDir = tempdir()
            val regularFile = project.layout.file(project.providers.ofNullable(File(baseDir, "nonexistent.txt")))

            val result = project.providers.checksum(regularFile, "SHA-256")

            result.orNull.shouldBeNull()
        }

        test("invalid algorithm returns absent") {
            val project = ProjectBuilder.builder().build()
            val file = tempfile()
            file.writeText("hello")
            val regularFile = project.layout.file(project.providers.ofNullable(file))

            val result = project.providers.checksum(regularFile, "NOT-A-REAL-ALGORITHM")

            result.orNull.shouldBeNull()
        }
    }
}
