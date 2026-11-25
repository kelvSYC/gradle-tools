package com.kelvsyc.gradle.ant.internal

import com.kelvsyc.gradle.ant.AntExecutionException
import com.kelvsyc.gradle.ant.TestAnt
import com.kelvsyc.gradle.ant.internal.AntShim.BuildFile
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.logging.Logger
import java.io.File

private fun File.write(name: String, body: String): File =
    File(this, name).apply {
        parentFile.mkdirs()
        writeText("<?xml version=\"1.0\"?>\n$body\n")
    }

class AntBuildShimSpec : FunSpec({
    fun read(files: List<File>, seeds: Map<String, String> = emptyMap()): AntShim.BuildModel =
        AntRuntimeClassLoader(TestAnt.classpath).use {
            AntShim(it).readBuild(files.map { file -> BuildFile(file, null) }, seeds, mockk(relaxed = true))
        }

    fun run(
        file: File,
        target: String,
        upstream: List<Map<String, String>> = emptyList(),
        logger: Logger = mockk(relaxed = true)
    ): Map<String, String> =
        AntRuntimeClassLoader(TestAnt.classpath).use {
            AntShim(it).runTarget(listOf(BuildFile(file, null)), emptyMap(), upstream, target, logger)
        }

    test("readBuild reports targets, descriptions, depends and Ant's upstream order") {
        val file = tempdir().write(
            "build.xml",
            """
            <project name="p">
                <target name="init"/>
                <target name="compile" depends="init"/>
                <target name="jar" depends="compile,init" description="Builds the jar"/>
            </project>
            """.trimIndent()
        )
        val model = read(listOf(file))
        val targets = model.targets.associateBy { it.name }
        targets.keys shouldContainExactlyInAnyOrder listOf("init", "compile", "jar")
        val jar = targets.getValue("jar")
        jar.description shouldBe "Builds the jar"
        jar.depends shouldBe listOf("compile", "init")
        jar.upstream shouldBe listOf("init", "compile")
        jar.file?.canonicalFile shouldBe file.canonicalFile
        targets.getValue("init").description shouldBe null
        targets.getValue("init").upstream shouldBe emptyList()
    }

    test("readBuild follows imports and reports loaded files") {
        val dir = tempdir()
        val common = dir.write(
            "common.xml",
            """
            <project name="common">
                <target name="clean"/>
            </project>
            """.trimIndent()
        )
        val main = dir.write(
            "build.xml",
            """
            <project name="main">
                <import file="common.xml"/>
                <target name="build" depends="clean"/>
            </project>
            """.trimIndent()
        )
        val model = read(listOf(main))
        model.loadedFiles.map { it.canonicalFile } shouldContainExactlyInAnyOrder listOf(main, common)
            .map { it.canonicalFile }
        model.targets.map { it.name } shouldContainAll listOf("build", "clean", "common.clean")
        model.targets.first { it.name == "clean" }.file?.canonicalFile shouldBe common.canonicalFile
        model.targets.first { it.name == "build" }.upstream shouldBe listOf("clean")
    }

    test("readBuild reports the existing files property and loadproperties read during the parse") {
        val dir = tempdir()
        File(dir, "build.properties").writeText("a=1\n")
        File(dir, "more.properties").writeText("b=2\n")
        File(dir, "imported.properties").writeText("c=3\n")
        dir.write(
            "common.xml",
            """
            <project name="common">
                <property file="imported.properties"/>
            </project>
            """.trimIndent()
        )
        val main = dir.write(
            "build.xml",
            """
            <project name="main">
                <property file="build.properties"/>
                <property file="missing.properties"/>
                <loadproperties srcFile="more.properties"/>
                <property name="plain" value="x"/>
                <import file="common.xml"/>
                <target name="t">
                    <property file="target-only.properties"/>
                </target>
            </project>
            """.trimIndent()
        )
        val model = read(listOf(main))
        model.readFiles.map { it.canonicalFile } shouldContainExactlyInAnyOrder
            listOf("build.properties", "more.properties", "imported.properties").map { File(dir, it).canonicalFile }
        model.topLevelProperties shouldContain ("c" to "3")
        model.loadedFiles.map { it.name } shouldContainExactlyInAnyOrder listOf("build.xml", "common.xml")
    }

    test("extension-point wiring shows in depends") {
        val file = tempdir().write(
            "build.xml",
            """
            <project name="p">
                <extension-point name="ready"/>
                <target name="prepare" extensionOf="ready"/>
            </project>
            """.trimIndent()
        )
        read(listOf(file)).targets.first { it.name == "ready" }.depends shouldBe listOf("prepare")
    }

    test("top-level properties honour seeds and first-wins") {
        val file = tempdir().write(
            "build.xml",
            """
            <project name="p">
                <property name="a" value="file"/>
                <property name="b" value="${'$'}{a}-x"/>
            </project>
            """.trimIndent()
        )
        val properties = read(listOf(file), mapOf("a" to "seed")).topLevelProperties
        properties shouldContain ("b" to "seed-x")
        properties shouldContain ("a" to "seed")
        properties shouldNotContainKey "basedir"
        properties shouldNotContainKey "ant.file"
        properties shouldNotContainKey "java.version"
    }

    test("a target name in two top-level files fails naming both") {
        val dir = tempdir()
        val first = dir.write("one.xml", "<project name=\"one\"><target name=\"same\"/></project>")
        val second = dir.write("two.xml", "<project name=\"two\"><target name=\"same\"/></project>")
        val exception = shouldThrow<AntExecutionException> { read(listOf(first, second)) }
        exception.message shouldContain "same"
        exception.message shouldContain first.name
        exception.message shouldContain second.name
    }

    test("a target name imported by one top-level file and defined by another fails naming both") {
        val dir = tempdir()
        val first = dir.write("one.xml", "<project name=\"one\"><target name=\"clean\"/></project>")
        dir.write("common.xml", "<project name=\"common\"><target name=\"clean\"/></project>")
        val second = dir.write(
            "two.xml",
            "<project name=\"two\"><import file=\"common.xml\"/><target name=\"b\"/></project>"
        )
        val exception = shouldThrow<AntExecutionException> { read(listOf(first, second)) }
        exception.message shouldContain "clean"
        exception.message shouldContain first.name
        exception.message shouldContain second.name
    }

    test("an importing file's target wins over an imported one within one top-level file") {
        val dir = tempdir()
        dir.write("common.xml", "<project name=\"common\"><target name=\"clean\"/></project>")
        val main = dir.write(
            "build.xml",
            "<project name=\"main\"><import file=\"common.xml\"/><target name=\"clean\"/></project>"
        )
        read(listOf(main)).targets.first { it.name == "clean" }.file?.canonicalFile shouldBe main.canonicalFile
    }

    test("a file imported by two top-level files is not a duplicate") {
        val dir = tempdir()
        dir.write("common.xml", "<project name=\"common\"><target name=\"clean\"/></project>")
        val first = dir.write("one.xml", "<project name=\"one\"><import file=\"common.xml\"/></project>")
        val second = dir.write("two.xml", "<project name=\"two\"><import file=\"common.xml\"/></project>")
        read(listOf(first, second)).targets.map { it.name } shouldContain "clean"
    }

    test("targets in Ant's core namespace count as defined") {
        val dir = tempdir()
        val first = dir.write("one.xml", "<project name=\"one\"><extension-point name=\"same\"/></project>")
        val second = dir.write(
            "two.xml",
            "<project xmlns=\"antlib:org.apache.tools.ant\" name=\"two\"><extension-point name=\"same\"/></project>"
        )
        val exception = shouldThrow<AntExecutionException> { read(listOf(first, second)) }
        exception.message shouldContain second.name
    }

    test("base directories follow each file") {
        val first = tempdir().write(
            "one/build.xml",
            "<project name=\"one\"><property name=\"one.here\" location=\".\"/><target name=\"a\"/></project>"
        )
        val second = tempdir().write(
            "two/build.xml",
            "<project name=\"two\"><property name=\"two.here\" location=\".\"/><target name=\"b\"/></project>"
        )
        val explicit = tempdir()
        val third = tempdir().write(
            "three/build.xml",
            "<project name=\"three\"><property name=\"three.here\" location=\".\"/></project>"
        )
        val fourDir = tempdir()
        val fourth = fourDir.write(
            "four/build.xml",
            "<project name=\"four\" basedir=\"..\"><property name=\"four.here\" location=\".\"/></project>"
        )
        val properties = AntRuntimeClassLoader(TestAnt.classpath).use {
            AntShim(it).readBuild(
                listOf(
                    BuildFile(first, null),
                    BuildFile(second, null),
                    BuildFile(third, explicit),
                    BuildFile(fourth, explicit)
                ),
                emptyMap(),
                mockk(relaxed = true)
            )
        }.topLevelProperties
        File(properties.getValue("one.here")).canonicalFile shouldBe first.parentFile.canonicalFile
        File(properties.getValue("two.here")).canonicalFile shouldBe second.parentFile.canonicalFile
        File(properties.getValue("three.here")).canonicalFile shouldBe explicit.canonicalFile
        File(properties.getValue("four.here")).canonicalFile shouldBe fourDir.canonicalFile
    }

    test("runTarget runs only the target and reports its new properties") {
        val file = tempdir().write(
            "build.xml",
            """
            <project name="p">
                <target name="init">
                    <echo message="init ran"/>
                    <property name="p" value="0"/>
                    <property name="fromInit" value="yes"/>
                </target>
                <target name="compile" depends="init">
                    <property name="q" value="${'$'}{p}"/>
                </target>
            </project>
            """.trimIndent()
        )
        val logger = mockk<Logger>(relaxed = true)
        val result = run(file, "compile", listOf(mapOf("p" to "1")), logger)
        result shouldBe mapOf("q" to "1")
        result shouldNotContainKey "fromInit"
        verify(exactly = 0) { logger.warn("[ant:echo] init ran") }
    }

    test("runTarget runs a target with the base directory and ant.file of its top-level file") {
        val first = tempdir().write(
            "one/build.xml",
            """
            <project name="one">
                <target name="a">
                    <property name="where" location="."/>
                    <property name="file" value="${'$'}{ant.file}"/>
                </target>
            </project>
            """.trimIndent()
        )
        val second = tempdir().write("two/build.xml", "<project name=\"two\"><target name=\"b\"/></project>")
        val result = AntRuntimeClassLoader(TestAnt.classpath).use {
            AntShim(it).runTarget(
                listOf(BuildFile(first, null), BuildFile(second, null)),
                emptyMap(),
                emptyList(),
                "a",
                mockk(relaxed = true)
            )
        }
        File(result.getValue("where")).canonicalFile shouldBe first.parentFile.canonicalFile
        File(result.getValue("file")).canonicalFile shouldBe first.canonicalFile
    }

    test("upstream exports are not visible to top-level statements") {
        val file = tempdir().write(
            "build.xml",
            """
            <project name="p">
                <condition property="x"><isset property="a"/></condition>
                <target name="t">
                    <property name="seenA" value="${'$'}{a}"/>
                    <property name="seenX" value="${'$'}{x}"/>
                </target>
            </project>
            """.trimIndent()
        )
        run(file, "t", listOf(mapOf("a" to "up"))) shouldBe mapOf("seenA" to "up", "seenX" to "${'$'}{x}")
    }

    test("runTarget honours if with upstream values") {
        val file = tempdir().write(
            "build.xml",
            """
            <project name="p">
                <target name="t" if="flag">
                    <property name="ran" value="yes"/>
                </target>
            </project>
            """.trimIndent()
        )
        run(file, "t") shouldBe emptyMap()
        run(file, "t", listOf(mapOf("flag" to "true"))) shouldBe mapOf("ran" to "yes")
    }

    test("upstream exports apply in order with first-definition-wins") {
        val file = tempdir().write(
            "build.xml",
            "<project name=\"p\"><target name=\"t\"><property name=\"seen\" value=\"${'$'}{v}\"/></target></project>"
        )
        run(file, "t", listOf(mapOf("v" to "first"), mapOf("v" to "second"))) shouldBe mapOf("seen" to "first")
    }

    test("a target that sets nothing returns an empty map") {
        val file = tempdir().write(
            "build.xml",
            """
            <project name="p">
                <property name="top" value="1"/>
                <target name="t"><echo message="hi"/></target>
            </project>
            """.trimIndent()
        )
        run(file, "t").shouldBeEmpty()
    }

    test("a failing target raises AntExecutionException with Ant's message") {
        val file = tempdir().write(
            "build.xml",
            "<project name=\"p\"><target name=\"t\"><fail message=\"boom\"/></target></project>"
        )
        val exception = shouldThrow<AntExecutionException> { run(file, "t") }
        exception.message shouldContain "boom"
    }

    test("an unknown target raises AntExecutionException") {
        val file = tempdir().write("build.xml", "<project name=\"p\"><target name=\"t\"/></project>")
        val exception = shouldThrow<AntExecutionException> { run(file, "missing") }
        exception.message shouldContain "missing"
    }

    test("depends on a name that is not a target is reported, and upstream skips it") {
        val file = tempdir().write(
            "build.xml",
            "<project name=\"p\"><target name=\"a\"/><target name=\"b\" depends=\"gradleTask,a\"/></project>"
        )
        val b = read(listOf(file)).targets.first { it.name == "b" }
        b.depends shouldBe listOf("gradleTask", "a")
        b.upstream shouldBe listOf("a")
    }
})
