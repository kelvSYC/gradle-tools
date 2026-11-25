package com.kelvsyc.gradle.ant

import groovy.lang.GroovyShell
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.api.Action
import org.gradle.kotlin.dsl.withGroovyBuilder

class AntTreesSpec : FunSpec({
    fun List<AntElement>.shimMaps() = map { it.resolve().toShimMap() }

    val expected = listOf(
        AntNode(
            "copy", null, mapOf("todir" to "out"), null,
            listOf(AntNode("fileset", null, mapOf("dir" to "src"), null, emptyList()))
        )
    ).map { it.toShimMap() }

    test("build returns the Kotlin DSL tree") {
        AntTrees.build(Action { task("copy", "todir" to "out") { element("fileset", "dir" to "src") } })
            .shimMaps() shouldBe expected
    }

    test("record with an Action records a withGroovyBuilder body") {
        AntTrees.record(Action { withGroovyBuilder { "copy"("todir" to "out") { "fileset"("dir" to "src") } } })
            .shimMaps() shouldBe expected
    }

    test("record with a Groovy closure runs it against the recorder, unchanged from a legacy ant block") {
        @Suppress("UNCHECKED_CAST")
        val elements = GroovyShell(AntTrees::class.java.classLoader).evaluate(
            """
            def source = 'src'
            com.kelvsyc.gradle.ant.AntTrees.record { copy(todir: 'out') { fileset(dir: source) } }
            """.trimIndent()
        ) as List<AntElement>
        elements.shimMaps() shouldBe expected
    }
})
