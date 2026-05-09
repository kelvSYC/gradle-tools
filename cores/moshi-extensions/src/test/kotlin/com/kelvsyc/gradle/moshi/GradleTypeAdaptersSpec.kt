package com.kelvsyc.gradle.moshi

import com.squareup.moshi.Moshi
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.net.URI

class GradleTypeAdaptersSpec : FunSpec() {
    init {
        val moshi = Moshi.Builder().addGradleTypeAdapters().build()

        context("UriAdapter") {
            test("serializes URI to string") {
                val adapter = moshi.adapter(URI::class.java)
                adapter.toJson(URI.create("https://example.com/path")) shouldBe "\"https://example.com/path\""
            }

            test("deserializes URI from string") {
                val adapter = moshi.adapter(URI::class.java)
                adapter.fromJson("\"https://example.com/path\"") shouldBe URI.create("https://example.com/path")
            }
        }

        context("FileAdapter") {
            test("serializes File to absolute path") {
                val adapter = moshi.adapter(File::class.java)
                val file = File("/tmp/test.txt")
                adapter.toJson(file) shouldBe "\"/tmp/test.txt\""
            }

            test("deserializes File from path string") {
                val adapter = moshi.adapter(File::class.java)
                adapter.fromJson("\"/tmp/test.txt\"") shouldBe File("/tmp/test.txt")
            }
        }
    }
}
