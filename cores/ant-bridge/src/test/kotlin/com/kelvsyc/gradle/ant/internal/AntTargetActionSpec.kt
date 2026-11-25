package com.kelvsyc.gradle.ant.internal

import com.kelvsyc.gradle.ant.AntExecutionException
import com.kelvsyc.gradle.ant.TestAnt
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class AntTargetActionSpec : FunSpec({
    fun action(
        buildFile: File,
        target: String,
        exportFile: File,
        upstream: List<File> = emptyList(),
        baseDirectories: List<String> = listOf("")
    ): AntTargetAction {
        val project = ProjectBuilder.builder().build()
        val params = project.objects.newInstance(AntTargetAction.Parameters::class.java)
        params.buildFiles.add(buildFile)
        params.baseDirectories.addAll(baseDirectories)
        params.upstreamExports.addAll(upstream.map { f -> project.layout.projectDirectory.file(f.absolutePath) })
        params.target.set(target)
        params.exportFile.set(exportFile)
        params.antClasspath.from(TestAnt.classpath)
        return object : AntTargetAction() {
            override fun getParameters() = params
        }
    }

    fun build(dir: File, body: String): File =
        dir.resolve("build.xml").apply { writeText("<project name=\"p\" default=\"a\">$body</project>") }

    test("writes the target's new properties to the export file") {
        val dir = tempdir()
        val file = build(dir, "<target name=\"a\"><property name=\"out\" value=\"v\"/></target>")
        val export = dir.resolve("out/a.properties")
        action(file, "a", export).execute()
        PropertiesFiles.read(export) shouldBe mapOf("out" to "v")
    }

    test("loads upstream exports in order") {
        val dir = tempdir()
        val first = dir.resolve("1.properties").also { PropertiesFiles.write(it, mapOf("x" to "first")) }
        val second = dir.resolve("2.properties").also { PropertiesFiles.write(it, mapOf("x" to "second")) }
        val file = build(dir, "<target name=\"a\"><property name=\"echo\" value=\"\${x}\"/></target>")
        val export = dir.resolve("a.properties")
        action(file, "a", export, listOf(first, second)).execute()
        PropertiesFiles.read(export)["echo"] shouldBe "first"
    }

    test("writes an empty export when the target defines nothing") {
        val dir = tempdir()
        val file = build(dir, "<target name=\"a\"/>")
        val export = dir.resolve("a.properties")
        action(file, "a", export).execute()
        PropertiesFiles.read(export) shouldBe emptyMap()
    }

    test("a failing target propagates AntExecutionException and leaves no export file") {
        val dir = tempdir()
        val file = build(dir, "<target name=\"a\"><fail message=\"boom\"/></target>")
        val export = dir.resolve("a.properties").also { PropertiesFiles.write(it, mapOf("stale" to "1")) }
        val e = shouldThrow<AntExecutionException> { action(file, "a", export).execute() }
        e.message shouldContain "boom"
        export.exists().shouldBeFalse()
    }

    test("a base directory with a space is the target's basedir") {
        val dir = tempdir()
        val base = dir.resolve("base dir").apply { mkdirs() }
        val file = build(dir, "<target name=\"a\"><property name=\"bd\" value=\"\${basedir}\"/></target>")
        val export = dir.resolve("a.properties")
        action(file, "a", export, baseDirectories = listOf(base.absolutePath)).execute()
        File(PropertiesFiles.read(export)["bd"]!!).canonicalFile shouldBe base.canonicalFile
    }

    test("mismatched buildFiles and baseDirectories fail before anything runs") {
        val dir = tempdir()
        val file = build(dir, "<target name=\"a\"/>")
        val export = dir.resolve("a.properties")
        val e = shouldThrow<IllegalArgumentException> {
            action(file, "a", export, baseDirectories = listOf("", "")).execute()
        }
        e.message shouldContain "same size (1 vs 2)"
        export.exists().shouldBeFalse()
    }
})
