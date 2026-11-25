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
    val awkward = mapOf("k" to "a=b:c#d\ne", "ü" to "é", "empty" to "", " lead" to "x", "euro" to "€")
    val dollars = mapOf("d1" to "a\$\$b", "d2" to "\${x}", "d3" to "\$")

    test("round-trips awkward values") {
        val file = File(tempdir(), "nested/out.properties")
        PropertiesFiles.write(file, awkward)
        PropertiesFiles.read(file) shouldBe awkward
    }

    test("round-trips dollar signs") {
        val file = File(tempdir(), "out.properties")
        PropertiesFiles.write(file, dollars)
        PropertiesFiles.read(file) shouldBe dollars
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

    test("Ant reads dollar signs literally") {
        val file = File(tempdir(), "out.properties")
        PropertiesFiles.write(file, dollars)
        val define = AntNode("property", null, mapOf("name" to "x", "value" to "X"), null, emptyList())
        val load = AntNode("property", null, mapOf("file" to file.absolutePath), null, emptyList())
        val result = AntRuntimeClassLoader(TestAnt.classpath).use {
            AntShim(it).execute(listOf(define, load), tempdir(), mockk<Logger>(relaxed = true), dollars.keys)
        }
        result.properties shouldBe dollars
    }
})
