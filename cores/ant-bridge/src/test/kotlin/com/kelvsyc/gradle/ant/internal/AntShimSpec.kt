package com.kelvsyc.gradle.ant.internal

import com.kelvsyc.gradle.ant.AntExecutionException
import com.kelvsyc.gradle.ant.AntNode
import com.kelvsyc.gradle.ant.TestAnt
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.logging.Logger
import java.io.File

private const val CONTRIB = "antlib:net.sf.antcontrib"

private fun node(
    name: String,
    vararg attributes: Pair<String, String>,
    namespace: String? = null,
    children: List<AntNode> = emptyList()
) = AntNode(name, namespace, linkedMapOf(*attributes), null, children)

class AntShimSpec : FunSpec({
    fun run(nodes: List<AntNode>, classpath: List<File> = TestAnt.classpath): Pair<Logger, File> {
        val logger = mockk<Logger>(relaxed = true)
        val baseDir = tempdir()
        AntRuntimeClassLoader(classpath).use { AntShim(it).execute(nodes, baseDir, logger) }
        return logger to baseDir
    }

    test("echo reaches the Gradle logger at warn") {
        val (logger, _) = run(listOf(node("echo", "message" to "hi")))
        verify { logger.warn("[ant:echo] hi") }
    }

    test("Ant comes from the bridge classpath") {
        AntRuntimeClassLoader(TestAnt.classpath).use {
            val location = it.loadClass("org.apache.tools.ant.Project").protectionDomain.codeSource.location
            location.path shouldEndWith "ant-1.10.18.jar"
        }
    }

    test("echo at level error reaches the Gradle logger at error") {
        val (logger, _) = run(listOf(node("echo", "message" to "bad", "level" to "error")))
        verify { logger.error("[ant:echo] bad") }
    }

    test("the loader sees neither Gradle nor the Kotlin stdlib") {
        AntRuntimeClassLoader(TestAnt.classpath).use {
            shouldThrow<ClassNotFoundException> { it.loadClass("org.gradle.api.Project") }
            shouldThrow<ClassNotFoundException> { it.loadClass("kotlin.Unit") }
        }
    }

    test("an incomplete Ant classpath fails with AntExecutionException, not a linkage error across the boundary") {
        val withoutLauncher = TestAnt.classpath.filterNot { it.name.startsWith("ant-launcher") }
        withoutLauncher.size shouldBe TestAnt.classpath.size - 1
        shouldThrow<AntExecutionException> {
            run(listOf(node("echo", "message" to "hi")), withoutLauncher)
        }.message shouldContain "AntMain"
    }

    test("basedir is the given directory") {
        val (logger, baseDir) = run(listOf(node("echo", "message" to "${'$'}{basedir}")))
        verify { logger.warn("[ant:echo] ${baseDir.absolutePath}") }
    }

    test("copy with nested fileset copies files") {
        val src = tempdir()
        File(src, "a.txt").writeText("a")
        File(src, "b.xml").writeText("b")
        val out = File(tempdir(), "out")
        val include = node("include", "name" to "**/*.txt")
        val fileset = node("fileset", "dir" to src.absolutePath, children = listOf(include))
        run(listOf(node("copy", "todir" to out.absolutePath, children = listOf(fileset))))
        File(out, "a.txt").exists() shouldBe true
        File(out, "b.xml").exists() shouldBe false
    }

    test("a failing task raises AntExecutionException with Ant's message") {
        val exception = shouldThrow<AntExecutionException> { run(listOf(node("fail", "message" to "boom"))) }
        exception.message shouldContain "boom"
    }

    test("taskdef from an antlib on the classpath") {
        val classpath = TestAnt.classpath + TestAnt.contribClasspath
        val (logger, _) = run(
            listOf(
                node("var", "name" to "x", "value" to "1", namespace = CONTRIB),
                node("echo", "message" to "${'$'}{x}")
            ),
            classpath
        )
        verify { logger.warn("[ant:echo] 1") }

        val equals = node("equals", "arg1" to "a", "arg2" to "a")
        val then = node("then", children = listOf(node("echo", "message" to "matched")))
        val (ifLogger, _) = run(
            listOf(
                node("taskdef", "resource" to "net/sf/antcontrib/antcontrib.properties"),
                node("if", children = listOf(equals, then))
            ),
            classpath
        )
        verify { ifLogger.warn("[ant:echo] matched") }
    }

    test("each execution starts from a fresh project") {
        val logger = mockk<Logger>(relaxed = true)
        val baseDir = tempdir()
        AntRuntimeClassLoader(TestAnt.classpath).use {
            val shim = AntShim(it)
            shim.execute(listOf(node("property", "name" to "x", "value" to "set")), baseDir, logger)
            shim.execute(listOf(node("echo", "message" to "${'$'}{x}")), baseDir, logger)
        }
        verify { logger.warn("[ant:echo] ${'$'}{x}") }
    }

    test("requested properties set by any task in the tree are returned") {
        val file = File(tempdir(), "present.txt").apply { writeText("x") }
        val nodes = listOf(
            node("property", "name" to "a", "value" to "1"),
            node("available", "file" to file.absolutePath, "property" to "b")
        )
        val result = AntRuntimeClassLoader(TestAnt.classpath).use {
            AntShim(it).execute(nodes, tempdir(), mockk<Logger>(relaxed = true), setOf("a", "b", "unset"))
        }
        result.properties shouldBe mapOf("a" to "1", "b" to "true")
    }

    test("no results requested returns no properties") {
        val result = AntRuntimeClassLoader(TestAnt.classpath).use {
            AntShim(it).execute(
                listOf(node("property", "name" to "a", "value" to "1")),
                tempdir(),
                mockk<Logger>(relaxed = true)
            )
        }
        result.properties shouldBe emptyMap()
    }
})
