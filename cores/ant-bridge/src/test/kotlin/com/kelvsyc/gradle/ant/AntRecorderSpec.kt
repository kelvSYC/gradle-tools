package com.kelvsyc.gradle.ant

import groovy.lang.Binding
import groovy.lang.Closure
import groovy.lang.GroovyShell
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.gradle.kotlin.dsl.withGroovyBuilder
import java.io.File

class AntRecorderSpec : FunSpec({
    fun recordGroovy(script: String, variables: Map<String, Any?> = emptyMap()): List<AntElement> =
        AntRecorder.record { recorder ->
            val binding = Binding()
            binding.setVariable("ant", recorder)
            variables.forEach { (name, value) -> binding.setVariable(name, value) }
            GroovyShell(binding).evaluate(script)
        }

    fun shouldBeCopyTree(elements: List<AntElement>) {
        elements shouldHaveSize 1
        val copy = elements.single()
        copy.name shouldBe "copy"
        copy.namespace shouldBe null
        copy.attributes shouldBe mapOf("todir" to "out")
        val fileset = copy.children.single()
        fileset.name shouldBe "fileset"
        fileset.attributes shouldBe mapOf("dir" to "src")
    }

    test("Groovy DSL call with map and closure") {
        val elements = recordGroovy("ant.copy(todir: 'out') { fileset(dir: 'src') { include(name: '**/*.xml') } }")
        shouldBeCopyTree(elements)
        val include = elements.single().children.single().children.single()
        include.name shouldBe "include"
        include.attributes shouldBe mapOf("name" to "**/*.xml")
    }

    test("text argument") {
        val elements = recordGroovy("ant.echo('hi')")
        elements.single().name shouldBe "echo"
        elements.single().text shouldBe "hi"
    }

    test("GString text and attributes are kept as strings") {
        val elements = recordGroovy("def who = 'you'; ant.echo(message: \"hi ${'$'}who\", \"to ${'$'}who\")")
        elements.single().attributes["message"] shouldBe "hi you"
        elements.single().text shouldBe "to you"
    }

    test("namespaced name") {
        val elements = recordGroovy("ant.'antlib:net.sf.antcontrib:var'(name: 'x', value: '1')")
        val element = elements.single()
        element.namespace shouldBe "antlib:net.sf.antcontrib"
        element.name shouldBe "var"
        element.attributes shouldBe mapOf("name" to "x", "value" to "1")
    }

    test("calls are recorded in order") {
        val elements = recordGroovy("ant.mkdir(dir: 'a'); ant.echo('b')")
        elements.map { it.name } shouldBe listOf("mkdir", "echo")
    }

    test("lazy values are kept") {
        val out = File("out")
        val elements = recordGroovy("ant.mkdir(dir: out)", mapOf("out" to out))
        elements.single().attributes["dir"] shouldBeSameInstanceAs out
    }

    test("closures resolve outer variables") {
        val elements = recordGroovy("def src = 'a'; ant.copy(todir: 'o') { fileset(dir: src) }")
        elements.single().children.single().attributes["dir"] shouldBe "a"
    }

    test("closures resolve owner properties") {
        val elements = recordGroovy("src = 'a'; ant.copy(todir: 'o') { fileset(dir: src) { include(name: src) } }")
        val fileset = elements.single().children.single()
        fileset.attributes["dir"] shouldBe "a"
        fileset.children.single().attributes["name"] shouldBe "a"
    }

    test("reading properties fails loudly") {
        shouldThrow<AntReadBackException> { recordGroovy("ant.properties['x']") }
            .message shouldContain "antBridge.properties"
        val exception = shouldThrow<AntReadBackException> { recordGroovy("ant.project") }
        exception.message shouldBe "Cannot read 'project' from a recorded Ant block: Ant runs after the block ends. " +
            "Load properties with antBridge.properties { } (a Provider) or read a task output instead."
    }

    test("reading recorder properties inside a closure fails loudly") {
        shouldThrow<AntReadBackException> { recordGroovy("ant.copy(todir: 'o') { fileset(dir: properties['x']) }") }
            .message shouldContain "antBridge.properties"
        shouldThrow<AntReadBackException> { recordGroovy("ant.copy(todir: 'o') { fileset(dir: missing) }") }
            .message shouldContain "'missing'"
    }

    test("setting properties fails loudly") {
        shouldThrow<AntReadBackException> { recordGroovy("ant.x = 1") }
    }

    test("using a returned value fails loudly") {
        val exception = shouldThrow<AntReadBackException> {
            recordGroovy("def r = ant.available(file: 'x'); r.toBoolean()")
        }
        exception.message shouldBe "The value returned by a recorded Ant call is not available: Ant runs after the block ends."
        shouldThrow<AntReadBackException> { recordGroovy("def r = ant.available(file: 'x'); if (r) { ant.echo('y') }") }
        shouldThrow<AntReadBackException> { recordGroovy("def r = ant.available(file: 'x'); r.value") }
        recordGroovy("def r = ant.available(file: 'x'); \"${'$'}r\"").single()
            .name shouldBe "available"
    }

    test("Object methods are not recorded") {
        val elements = recordGroovy(
            "assert ant.toString() != null; assert ant.hashCode() == ant.hashCode(); assert ant.equals(ant)"
        )
        elements shouldBe emptyList()
    }

    test("Groovy closure with the recorder as delegate records") {
        val closure = GroovyShell().evaluate("return { copy(todir: 'out') { fileset(dir: 'src') } }") as Closure<*>
        val elements = AntRecorder.record { recorder ->
            val body = closure.clone() as Closure<*>
            body.resolveStrategy = Closure.DELEGATE_FIRST
            body.delegate = recorder
            body.call(recorder)
        }
        shouldBeCopyTree(elements)
    }

    test("Kotlin withGroovyBuilder body records the same tree") {
        val elements = AntRecorder.record { recorder ->
            recorder.withGroovyBuilder {
                "copy"("todir" to "out") {
                    "fileset"("dir" to "src")
                }
            }
        }
        shouldBeCopyTree(elements)
    }
})
