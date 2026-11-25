package com.kelvsyc.gradle.ant.internal

import com.kelvsyc.gradle.ant.AntNode
import com.kelvsyc.gradle.ant.TestAnt
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.gradle.api.logging.Logger
import java.io.File

class PropertiesFilesSpec : FunSpec({
    val awkward = mapOf("k" to "a=b:c#d\ne", "ü" to "é")

    test("round-trips awkward values") {
        val file = File(tempdir(), "nested/out.properties")
        PropertiesFiles.write(file, awkward)
        PropertiesFiles.read(file) shouldBe awkward
    }

    test("output is deterministic") {
        val dir = tempdir()
        val first = File(dir, "a.properties")
        val second = File(dir, "b.properties")
        val values = mapOf("z" to "1", "a" to "2", "m" to "3")
        PropertiesFiles.write(first, values)
        PropertiesFiles.write(second, values)
        first.readBytes() shouldBe second.readBytes()
        first.readLines().none { it.startsWith("#") } shouldBe true
        first.readLines().map { it.substringBefore('=') } shouldBe listOf("a", "m", "z")
    }

    test("Ant reads the file back") {
        val file = File(tempdir(), "out.properties")
        PropertiesFiles.write(file, awkward)
        val node = AntNode("property", null, mapOf("file" to file.absolutePath), null, emptyList())
        val result = AntRuntimeClassLoader(TestAnt.classpath).use {
            AntShim(it).execute(listOf(node), tempdir(), mockk<Logger>(relaxed = true), awkward.keys)
        }
        result.properties shouldBe awkward
    }
})
