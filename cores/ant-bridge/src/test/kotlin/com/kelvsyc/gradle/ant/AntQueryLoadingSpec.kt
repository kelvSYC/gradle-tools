package com.kelvsyc.gradle.ant

import com.kelvsyc.gradle.plugins.AntBridgePlugin
import com.kelvsyc.gradle.providers.getting
import groovy.lang.Binding
import groovy.lang.GroovyShell
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class AntQueryLoadingSpec : FunSpec({
    fun project(): Project = ProjectBuilder.builder().withProjectDir(tempdir()).build()
        .also { it.plugins.apply(AntBridgePlugin::class.java) }

    fun Project.query(configure: AntQuerySpec.() -> Unit): Provider<Map<String, String>> {
        val spec = objects.newInstance(AntQuerySpec::class.java).apply(configure)
        return providers.of(AntQuery::class.java) {
            parameters {
                spec.applyTo(this)
                antClasspath.setFrom(TestAnt.classpath)
            }
        }
    }

    test("a query returns the properties its tree sets") {
        val project = project()
        val present = File(project.projectDir, "present.txt").also { it.writeText("hello") }
        val provider = project.query {
            ant {
                task("available", "file" to present, "property" to "hasIt")
                task("loadfile", "srcFile" to present, "property" to "content")
            }
            result("hasIt", "content", "unset")
        }
        provider.get() shouldBe mapOf("hasIt" to "true", "content" to "hello")
        provider.getting("unset").isPresent shouldBe false
    }

    test("each allowed task passes the allowlist") {
        val project = project()
        val allowed = listOf(
            "available", "condition", "loadfile", "loadproperties", "property", "basename", "dirname",
            "pathconvert", "length"
        )
        allowed.forEach { name -> project.query { ant { task(name) } } }
        project.query { ant { task("checksum", "file" to "f", "property" to "sum") } }
    }

    test("disallowed tasks fail when the query is declared") {
        val project = project()
        val target = File(project.projectDir, "copied")
        val source = File(project.projectDir, "source.txt").also { it.writeText("x") }
        val cases = listOf<Pair<String, AntQuerySpec.() -> Unit>>(
            "exec" to { ant { task("exec", "executable" to "true") } },
            "copy" to { ant { task("copy", "file" to source, "todir" to target) } },
            "tstamp" to { ant { task("tstamp") } },
            "checksum" to { ant { task("checksum", "file" to source) } },
            "if" to { ant { task("if", namespace = "antlib:net.sf.antcontrib") } }
        )
        cases.forEach { (name, configure) ->
            val error = shouldThrow<InvalidUserDataException> { project.query(configure) }
            error.message shouldContain "Ant task '$name'"
            error.message shouldContain "allowSideEffects()"
        }
    }

    test("the allowlist failure explains the re-run and points to AntWork.exportProperties") {
        val error = shouldThrow<InvalidUserDataException> { project().query { ant { task("exec") } } }
        error.message shouldContain "re-runs"
        error.message shouldContain "AntWork.exportProperties"
    }

    test("nested elements of an allowed task are not checked") {
        // `istrue` is not on the allowlist, but as a nested condition it is the allowed task's configuration.
        val provider = project().query {
            ant { task("condition", "property" to "p") { element("istrue", "value" to "true") } }
            result("p")
        }
        provider.get() shouldBe mapOf("p" to "true")
    }

    test("allowSideEffects lifts the allowlist") {
        val project = project()
        val target = File(project.projectDir, "copied")
        val source = File(project.projectDir, "source.txt").also { it.writeText("x") }
        val provider = project.query {
            allowSideEffects()
            ant { task("copy", "file" to source, "todir" to target) }
        }
        provider.get()
        File(target, "source.txt").exists() shouldBe true
    }

    test("an Ant failure fails the query with Ant's message") {
        val project = project()
        val missing = File(project.projectDir, "missing.txt")
        val provider = project.query {
            ant { task("loadfile", "srcFile" to missing, "property" to "x", "failonerror" to "true") }
            result("x")
        }
        val error = shouldThrow<Exception> { provider.get() }
        val antError = generateSequence<Throwable>(error) { it.cause }.filterIsInstance<AntExecutionException>().first()
        antError.message shouldContain "missing.txt"
    }

    test("a Groovy closure query records legacy calls") {
        val project = project()
        val present = File(project.projectDir, "present.txt").also { it.writeText("hello") }
        val spec = project.objects.newInstance(AntQuerySpec::class.java)
        val binding = Binding().apply {
            setVariable("spec", spec)
            setVariable("f", present)
        }
        GroovyShell(AntQuerySpec::class.java.classLoader, binding).evaluate(
            """
            spec.record { available(file: f, property: 'p') }
            spec.result('p')
            """.trimIndent()
        )
        val provider = project.providers.of(AntQuery::class.java) {
            parameters {
                spec.applyTo(this)
                antClasspath.setFrom(TestAnt.classpath)
            }
        }
        provider.get() shouldBe mapOf("p" to "true")
    }

    test("antBridge.query wires the extension's classpath") {
        val project = project()
        project.repositories.mavenCentral()
        val extension = project.extensions.getByType(AntBridgeExtension::class.java)
        val provider = extension.query { ant { task("property", "name" to "k", "value" to "v") }; result("k") }
        provider.get() shouldBe mapOf("k" to "v")
    }
})
