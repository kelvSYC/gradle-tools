package com.kelvsyc.gradle.ant.internal

import com.kelvsyc.gradle.ant.AntExecutionException
import com.kelvsyc.gradle.ant.AntNode
import com.kelvsyc.gradle.ant.TestAnt
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.logging.Logger
import java.io.File
import java.nio.file.Files

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
        val baseDir = Files.createTempDirectory("ant shim").toFile()
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

    test("basedir is the given directory") {
        val (logger, baseDir) = run(listOf(node("echo", "message" to "${'$'}{basedir}")))
        verify { logger.warn("[ant:echo] ${baseDir.absolutePath}") }
    }

    test("copy with nested fileset copies files") {
        val src = Files.createTempDirectory("ant src").toFile()
        File(src, "a.txt").writeText("a")
        File(src, "b.xml").writeText("b")
        val out = File(Files.createTempDirectory("ant out").toFile(), "out")
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
        val baseDir = Files.createTempDirectory("ant shim").toFile()
        AntRuntimeClassLoader(TestAnt.classpath).use {
            val shim = AntShim(it)
            shim.execute(listOf(node("property", "name" to "x", "value" to "set")), baseDir, logger)
            shim.execute(listOf(node("echo", "message" to "${'$'}{x}")), baseDir, logger)
        }
        verify { logger.warn("[ant:echo] ${'$'}{x}") }
    }
})
